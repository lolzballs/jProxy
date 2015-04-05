package tk.jackyliao123.proxy.server;

import tk.jackyliao123.nioevent.*;
import tk.jackyliao123.proxy.AESCipher;
import tk.jackyliao123.proxy.Constants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    public final Server server;
    public final EventProcessor processor;
    public final SocketChannel clientSocket;
    public final String username;
    private final AESCipher cipher;
    private final SocketChannel[] clientConnections;
    private final DeathEventHandler clientDeath;
    private final DeathEventHandler connectionDeath;

    public Connection(Server server, SocketChannel channel, String username, SecretKey key) throws Exception {
        this.server = server;
        this.processor = server.processor;
        this.clientSocket = channel;
        this.username = username;
        this.cipher = new AESCipher(key);
        this.clientConnections = new SocketChannel[Constants.MAX_CONNECTIONS];
        this.clientDeath = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                for (SocketChannel socket : clientConnections) {
                    if (socket != null) {
                        socket.close();
                    }
                }
                Connection.this.server.connections.remove(Connection.this);
            }
        };
        this.connectionDeath = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                clientConnections[event.id] = null;
            }
        };

        readPacket();
    }

    private void readPacket() throws IOException {
        server.processor.register(clientSocket, 1, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                readEncrypted(bytes[0]);
            }
        }, clientDeath);
    }

    private void readEncrypted(int size) throws IOException {
        server.processor.register(clientSocket, size * 16, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                byte[] data = cipher.decrypt(bytes);

                if (data == null) {
                    return;
                }

                byte magic = data[0];
                switch (magic) {
                    case Constants.CONNECTION_TCP:
                        connectTCP(data);
                        break;
                    case Constants.TYPE_PING:
                        break;
                    case Constants.TYPE_TCP:
                        sendTCP(data);
                        break;
                    case Constants.TYPE_UDP:
                        break;
                }

                readPacket();
            }
        }, clientDeath);
    }

    private boolean sendEncrypted(SocketChannel channel, byte[] data) throws IOException {
        byte[] encrpyted = cipher.encrypt(data);
        if (encrpyted == null) {
            return false;
        }

        ByteBuffer send = ByteBuffer.allocate(1 + encrpyted.length);
        send.put((byte) (encrpyted.length / 16));
        send.put(encrpyted);

        send.flip();
        return channel.write(send) == send.capacity();
    }

    private void connectTCP(byte[] data) throws IOException {
        byte[] ip = new byte[]{data[1], data[2], data[3], data[4]};
        int port = (data[5] << 8) | data[6];
        final byte id1 = data[7];
        final byte id2 = data[8];

        InetAddress address = InetAddress.getByAddress(ip);

        SocketChannel socket = SocketChannel.open();
        socket.configureBlocking(false);
        socket.connect(new InetSocketAddress(address, port));

        processor.notifyConnection(socket, new ConnectEventHandler() {
            @Override
            public void action(SocketChannel socket, boolean connectable) throws IOException {
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

                sendEncrypted(clientSocket, new byte[]{Constants.CONNECTION_TCP, status, id1, id2});
                int id = (id1 << 8) | id2;

                if (status == Constants.TCP_CONNECTION_OK) {
                    clientConnections[id] = socket;
                    readTCP(id);
                }
            }
        });
    }

    private void readTCP(final int id) throws IOException {
        if (clientConnections[id] == null) {
            return;
        }
        server.processor.register(id, clientConnections[id], new ReadEventHandler() {
            @Override
            public void action(EventProcess process, SocketChannel channel, byte[] bytes) throws IOException {
                byte[] data = new byte[bytes.length + 5];
                data[0] = Constants.TYPE_TCP;
                data[1] = (byte) (id >> 8);
                data[2] = (byte) (id & 0xFF);
                data[3] = (byte) (bytes.length >> 8);
                data[4] = (byte) (bytes.length & 0xFF);
                System.arraycopy(bytes, 0, data, 5, bytes.length);
                sendEncrypted(clientSocket, data);

                readTCP(id);
            }
        }, connectionDeath);
    }

    private void sendTCP(byte[] data) throws IOException {
        int id = (data[1] << 8) | data[2];
        int payloadLength = (data[3] << 8) | data[4];
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, 5, payload, 0, payloadLength);

        if (clientConnections[id] == null) {
            return;
        }
        clientConnections[id].write(ByteBuffer.wrap(payload));
    }
}

