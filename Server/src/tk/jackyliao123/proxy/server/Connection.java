package tk.jackyliao123.proxy.server;

import tk.jackyliao123.nioevent.*;
import tk.jackyliao123.proxy.AESCipher;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.tcp.TCPHandler;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
    public final DeathEventHandler clientDeath;
    public final DeathEventHandler connectionDeath;

    private final Server server;
    private final EventProcessor processor;
    private final SocketChannel client;
    private final String username;
    private final AESCipher cipher;
    private final TCPHandler tcp;

    public Connection(Server server, SocketChannel channel, String username, SecretKey key) throws Exception {
        this.clientDeath = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                tcp.clientDeath();
                Connection.this.server.connections.remove(Connection.this);
            }
        };
        this.connectionDeath = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                tcp.death(event.id);
            }
        };

        this.server = server;
        this.processor = server.processor;
        this.client = channel;
        this.username = username;
        this.cipher = new AESCipher(key);
        this.tcp = new TCPHandler(this, processor, client);

        readPacket();
    }

    public boolean sendEncrypted(SocketChannel channel, byte[] data) throws IOException {
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

    private void readPacket() throws IOException {
        server.processor.register(client, 1, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                readEncrypted(bytes[0]);
            }
        }, clientDeath);
    }

    private void readEncrypted(int size) throws IOException {
        server.processor.register(client, size * 16, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                byte[] data = cipher.decrypt(bytes);

                if (data == null) {
                    return;
                }

                byte magic = data[0];
                switch (magic) {
                    case Constants.CONNECTION_TCP:
                        tcp.connect(data);
                        break;
                    case Constants.TYPE_PING:
                        break;
                    case Constants.TYPE_TCP:
                        tcp.send(data);
                        break;
                    case Constants.TYPE_UDP:
                        break;
                }

                readPacket();
            }
        }, clientDeath);
    }
}

