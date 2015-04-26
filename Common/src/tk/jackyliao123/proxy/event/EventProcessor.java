package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.BufferFiller;
import tk.jackyliao123.proxy.ChannelWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class EventProcessor {
    protected Selector selector;

    public EventProcessor() throws IOException {
        this.selector = Selector.open();
    }

    public void fillArrayToMax(ChannelWrapper channel, int arraySize, ReadEventListener listener) throws IOException {
        channel.pushFillReadBuffer(ByteBuffer.allocate(arraySize), listener);
    }

    public void fillArray(ChannelWrapper channel, ReadEventListener listener) throws IOException {
        channel.pushDumpReadBuffer(listener);
    }

    public Selector getSelector() {
        return selector;
    }

    public void process(long timeout) throws IOException {
        selector.select(timeout);

        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            try {
                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    continue;
                }

                if (key.isReadable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelWrapper) {
                        ChannelWrapper channel = (ChannelWrapper) attachment;
                        BufferFiller b = channel.getReadBuffer();
                        if (b != null) {
                            int read = channel.read(b.buffer);
                            if (read == -1) {
                                kill(channel);
                            }

                            if (channel.isFullyRead()) {
                                channel.popReadBuffer().listener.onRead(channel, b.buffer.array());
                            } else if (!b.fillToMax) {
                                byte[] array = new byte[b.buffer.position()];
                                System.arraycopy(b.buffer.array(), 0, array, 0, b.buffer.position());
                                channel.popReadBuffer().listener.onRead(channel, array);
                            }
                        } else {
                            System.err.println("Error, ignored bytes, buffered. Performance bug. This should not happen.");
                        }
                    }
                }

                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    continue;
                }

                if (key.isWritable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof ChannelWrapper) {
                        ChannelWrapper channel = (ChannelWrapper) attachment;
                        ByteBuffer b = channel.getWriteBuffer();
                        if (b != null) {
                            int write = channel.write(b);

                            if (channel.isFullyWritten()) {
                                channel.popWriteBuffer();
                            }
                        }
                    }
                }

                if (!key.isValid()) {
                    kill((ChannelWrapper) key.attachment());
                    continue;
                }

                if (key.isAcceptable()) {
                    Object attachment = key.attachment();
                    if (attachment != null && attachment instanceof AcceptEventListener) {
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        SelectionKey nKey = channel.register(selector, 0);
                        ChannelWrapper wrapper = new ChannelWrapper(channel, nKey);
                        nKey.attach(wrapper);
                        ((AcceptEventListener) attachment).onAccept(wrapper);
                    }
                }
            } catch (Exception e) {
                System.err.println("Event processing has experienced an error on " + key);
                e.printStackTrace();
            }
            keys.remove();
        }
    }

    public void registerServerChannel(ServerSocketChannel channel, AcceptEventListener listener) throws IOException {
        channel.register(selector, SelectionKey.OP_ACCEPT, listener);
    }

    public void kill(ChannelWrapper channel) {
        try {
            channel.close();
        } catch (IOException e) {
            System.err.println("Unable to kill connection");
            e.printStackTrace();
        }

        System.out.println("Connection closed: " + channel.channel);
    }
}
