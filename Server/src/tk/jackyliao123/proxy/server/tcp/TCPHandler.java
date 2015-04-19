package tk.jackyliao123.proxy.server.tcp;

import tk.jackyliao123.nioevent.ConnectEventHandler;
import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;

public class TCPHandler {
    private final Connection server;
    private final EventProcessor processor;
    private final SocketChannel client;
    private final TCPConnection[] connections;

    public TCPHandler(Connection server, EventProcessor processor, SocketChannel client) {
        this.server = server;
        this.processor = processor;
        this.client = client;

        this.connections = new TCPConnection[Constants.MAX_CONNECTIONS];
    }

    public void connect(byte[] data) throws IOException {
        final byte[] ip = new byte[]{data[1], data[2], data[3], data[4]};
        final int port = (data[5] << 8) + (data[6] & 0xFF);
        final byte id1 = data[7];
        final byte id2 = data[8];

        InetAddress address = InetAddress.getByAddress(ip);

        SocketChannel socket = SocketChannel.open();
        socket.configureBlocking(false);
        socket.connect(new InetSocketAddress(address, port));

        processor.notifyConnection(socket, new ConnectEventHandler() {
            @Override
            public void action(AbstractSelectableChannel channel, boolean connectable) throws IOException {
                SocketChannel socket = (SocketChannel) channel;
                byte status;
                try {
                    socket.finishConnect();
                    if (connectable) {
                        status = Constants.TCP_CONNECTION_OK;
                    } else {
                        status = Constants.TCP_CONNECTION_TIMEOUT;
                    }
                } catch (SocketException e) {
                    String message = e.getMessage();
                    if (message.equals("Network is unreachable")) {
                        status = Constants.TCP_CONNECTION_UNREACHABLE;
                    } else if (message.equals("Connection refused")) {
                        status = Constants.TCP_CONNECTION_REFUSED;
                    } else {
                        System.out.println("Unknown TCP connection error: " + e.getMessage());
                        status = Constants.TCP_CONNECTION_OTHER;
                    }
                }

                server.sendEncrypted(client, new byte[]{Constants.CONNECTION_TCP, status, id1, id2});
                int id = (id1 << 8) + (id2 & 0xFF);

                if (status == Constants.TCP_CONNECTION_OK) {
                    connections[id] = new TCPConnection(id, ip, port, socket);
                    read(id);
                }
            }
        });
    }

    public void send(byte[] data) throws IOException {
        int id = (data[1] << 8) + (data[2] & 0xFF);
        int payloadLength = (data[3] << 8) + (data[4] & 0xFF);
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, 5, payload, 0, payloadLength);

        if (connections[id] == null) {
            return;
        }
        connections[id].socket.write(ByteBuffer.wrap(payload));
    }

    private void read(final int id) throws IOException {
        if (connections[id] == null) {
            return;
        }
        processor.register(id, connections[id].socket, new ReadEventHandler() {
            @Override
            public void action(EventProcess process, SocketChannel channel, byte[] bytes) throws IOException {
                byte[] data = new byte[bytes.length + 5];
                data[0] = Constants.TYPE_TCP;
                data[1] = (byte) (id >>> 8);
                data[2] = (byte) (id & 0xFF);
                data[3] = (byte) (bytes.length >>> 8);
                data[4] = (byte) (bytes.length & 0xFF);
                System.arraycopy(bytes, 0, data, 5, bytes.length);
                server.sendEncrypted(client, data);

                read(id);
            }
        }, server.connectionDeath);
    }

    public void clientDeath() {
        for (TCPConnection connection : connections) {
            if (connection == null) {
                continue;
            }
            try {
                connection.socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void death(int id) throws IOException {
        int port = connections[id].port;
        connections[id] = null;

        server.sendEncrypted(client, new byte[]{Constants.DISCONNECT_TCP, (byte) (id >>> 8), (byte) (id & 0xFF)});
    }
}
