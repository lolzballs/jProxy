package tk.jackyliao123.nioevent;

import tk.jackyliao123.proxy.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class EventProcessor {
    private static final long DEFAULT_TIMEOUT = 60000000000L;

    private HashMap<SocketChannel, ConnectionProcess> waitForConnect;
    private HashMap<SocketChannel, EventProcess> channels;
    private Selector selector;

    public EventProcessor(Selector selector) throws IOException {
        this.waitForConnect = new HashMap<SocketChannel, ConnectionProcess>();
        this.channels = new HashMap<SocketChannel, EventProcess>();
        this.selector = selector;
    }

    public void pollEvents() throws IOException {
        checkForNonConnected();
        checkForDeadConnections();
        selector.select(DEFAULT_TIMEOUT / 1000000L);

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (!key.isValid()) {
                keyIterator.remove();
                continue;
            }

            if (key.isConnectable()) {
                connect(key);
                keyIterator.remove();

                continue;
            }

            if (key.isReadable()) {
                try {
                    read(key);
                } catch (IOException e) {
                    e.printStackTrace();
                    killConnection((SocketChannel) key.channel());
                }

                keyIterator.remove();
                continue;
            }
        }
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        key.interestOps(0);
        waitForConnect.remove(channel).handler.action(channel, true);
    }

    private void checkForNonConnected() throws IOException {
        ArrayList<SocketChannel> dead = new ArrayList<SocketChannel>();
        for (Map.Entry<SocketChannel, ConnectionProcess> entry : waitForConnect.entrySet()) {
            long current = System.nanoTime();
            long time = current - entry.getValue().startTime;
            if (time > entry.getValue().timeout) {
                entry.getValue().handler.action(entry.getKey(), false);
                dead.add(entry.getKey());
            }
        }

        for (SocketChannel channel : dead) {
            waitForConnect.remove(channel);
        }
    }

    private void checkForDeadConnections() {
        ArrayList<SocketChannel> dead = new ArrayList<SocketChannel>();
        for (Map.Entry<SocketChannel, EventProcess> entry : channels.entrySet()) {
            long current = System.nanoTime();
            long time = current - entry.getValue().lastTime;
            if (time > entry.getValue().timeout) {
                dead.add(entry.getKey());
            }
        }

        for (SocketChannel channel : dead) {
            killConnection(channel);
        }
    }

    private void killConnection(SocketChannel channel) {
        try {
            System.out.println("Killing " + channel.getRemoteAddress());
            EventProcess process = channels.remove(channel);
            process.death.action(process);
            channel.close();
        } catch (IOException ignored) {
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        EventProcess event = channels.get(channel);

        ByteBuffer buffer = event.buffer;
        ReadEventHandler handler = event.read;

        int read = channel.read(buffer);

        if (read == -1) {
            System.out.print("Closed socket! ");
            killConnection((SocketChannel) key.channel());
            key.cancel();
            return;
        }

        if (read != 0) {
            event.lastTime = System.nanoTime();
        }

        if (!buffer.hasRemaining() || event.notifyOnRead) {
            try {
                unregisterEventListener(key);
                if (event.notifyOnRead) {
                    byte[] data = new byte[read];
                    System.arraycopy(buffer.array(), 0, data, 0, read);
                    handler.action(channels.get(channel), channel, data);
                } else {
                    handler.action(channels.get(channel), channel, buffer.array());
                }
            } catch (IOException e) {
                channel.close();
            }
        }
    }

    public void notifyConnection(SocketChannel channel, ConnectEventHandler handler) throws IOException {
        notifyConnection(channel, handler, DEFAULT_TIMEOUT);
    }

    public void notifyConnection(SocketChannel channel, ConnectEventHandler handler, long timeout) throws IOException {
        channel.register(selector, SelectionKey.OP_CONNECT);
        waitForConnect.put(channel, new ConnectionProcess(handler, System.nanoTime(), timeout));
    }

    public void register(int id, SocketChannel channel, ReadEventHandler read) throws IOException {
        register(id, channel, read, new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
            }
        }, DEFAULT_TIMEOUT);
    }

    public void register(int id, SocketChannel channel, ReadEventHandler read, DeathEventHandler death) throws IOException {
        register(id, channel, read, death, DEFAULT_TIMEOUT);
    }

    public void register(int id, SocketChannel channel, ReadEventHandler read, DeathEventHandler death, long timeout) throws IOException {
        channel.configureBlocking(false);
        EventProcess entry = channels.get(channel);
        if (entry != null) {
            entry.buffer = ByteBuffer.allocate(Constants.MAX_MTU);
            entry.read = read;
            entry.death = death;
            entry.notifyOnRead = true;
            entry.id = id;
            entry.lastTime = System.nanoTime();
            entry.key.interestOps(SelectionKey.OP_READ);
            return;
        }
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        EventProcess event = new EventProcess(key, read, death, ByteBuffer.allocate(Constants.MAX_MTU), true, timeout, id);
        event.lastTime = System.nanoTime();
        channels.put(channel, event);
    }

    public void register(SocketChannel channel, ReadEventHandler read) throws IOException {
        register(channel, read, new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
            }
        }, DEFAULT_TIMEOUT);
    }

    public void register(SocketChannel channel, ReadEventHandler read, DeathEventHandler death) throws IOException {
        register(channel, read, death, DEFAULT_TIMEOUT);
    }

    public void register(SocketChannel channel, ReadEventHandler read, DeathEventHandler death, long timeout) throws IOException {
        channel.configureBlocking(false);
        EventProcess entry = channels.get(channel);
        if (entry != null) {
            entry.buffer = ByteBuffer.allocate(Constants.MAX_MTU);
            entry.read = read;
            entry.death = death;
            entry.notifyOnRead = true;
            entry.lastTime = System.nanoTime();
            entry.key.interestOps(SelectionKey.OP_READ);
            return;
        }
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        EventProcess event = new EventProcess(key, read, death, ByteBuffer.allocate(Constants.MAX_MTU), true, timeout);
        event.lastTime = System.nanoTime();
        channels.put(channel, event);
    }

    public void register(SocketChannel channel, int bytesToRead, ReadEventHandler read) throws IOException {
        register(channel, bytesToRead, read, new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
            }
        });
    }

    public void register(SocketChannel channel, int bytesToRead, ReadEventHandler read, DeathEventHandler death) throws IOException {
        register(channel, bytesToRead, read, death, DEFAULT_TIMEOUT);
    }

    public void register(SocketChannel channel, int bytesToRead, ReadEventHandler read, DeathEventHandler death, long timeout) throws IOException {
        channel.configureBlocking(false);
        EventProcess entry = channels.get(channel);
        if (entry != null) {
            entry.buffer = ByteBuffer.allocate(bytesToRead);
            entry.read = read;
            entry.death = death;
            entry.lastTime = System.nanoTime();
            entry.key.interestOps(SelectionKey.OP_READ);
            return;
        }
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        EventProcess event = new EventProcess(key, read, death, ByteBuffer.allocate(bytesToRead), false, timeout);
        event.lastTime = System.nanoTime();
        channels.put(channel, event);
    }

    public void register(int id, SocketChannel channel, int bytesToRead, ReadEventHandler read) throws IOException {
        register(id, channel, bytesToRead, read, new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
            }
        });
    }

    public void register(int id, SocketChannel channel, int bytesToRead, ReadEventHandler read, DeathEventHandler death) throws IOException {
        register(id, channel, bytesToRead, read, death, DEFAULT_TIMEOUT);
    }

    public void register(int id, SocketChannel channel, int bytesToRead, ReadEventHandler read, DeathEventHandler death, long timeout) throws IOException {
        channel.configureBlocking(false);
        EventProcess entry = channels.get(channel);
        if (entry != null) {
            entry.buffer = ByteBuffer.allocate(bytesToRead);
            entry.read = read;
            entry.death = death;
            entry.id = id;
            entry.lastTime = System.nanoTime();
            entry.key.interestOps(SelectionKey.OP_READ);
            return;
        }
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
        EventProcess event = new EventProcess(key, read, death, ByteBuffer.allocate(bytesToRead), false, timeout, id);
        event.lastTime = System.nanoTime();
        channels.put(channel, event);
    }

    public void deleteEventListener(SocketChannel channel) {
        EventProcess process = channels.get(channel);
        process.key.cancel();
        channels.remove(channel);
    }

    public void unregisterEventListener(SelectionKey key) {
        if (key.isValid()) {
            key.interestOps(0);
        }
    }
}
