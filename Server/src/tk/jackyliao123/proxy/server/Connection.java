package tk.jackyliao123.proxy.server;

import tk.jackyliao123.nioevent.DeathEventHandler;
import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.AESCipher;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.dns.DNSHandler;
import tk.jackyliao123.proxy.server.tcp.TCPHandler;
import tk.jackyliao123.proxy.server.udp.UDPHandler;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
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
    private final UDPHandler udp;
    private final DNSHandler dns;

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
                tcp.death((Integer) event.info);
            }
        };

        this.server = server;
        this.processor = server.processor;
        this.client = channel;
        this.username = username;
        this.cipher = new AESCipher(key);
        this.tcp = new TCPHandler(this, processor, client);
        this.udp = new UDPHandler(this, processor, client);
        this.dns = new DNSHandler(this, processor, client);

        readPacket();
    }

    public synchronized boolean sendEncrypted(SocketChannel channel, byte[] data) throws IOException {
        byte[] encrypted = cipher.encrypt(data);
        if (encrypted == null) {
            return false;
        }

        ByteBuffer send = ByteBuffer.allocate(1 + encrypted.length);
        send.put((byte) (encrypted.length / 16));
        send.put(encrypted);

        send.flip();

        // TODO: MAKE MORE EFFICENT
        int written = 0;
        while (send.hasRemaining()) {
            written += channel.write(send);
        }
        return written == send.capacity();
    }

    private void readPacket() throws IOException {
        server.processor.register(client, 1, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, ByteChannel channel, byte[] bytes) throws IOException {
                readEncrypted(bytes[0] & 0xFF);
            }
        }, clientDeath);
    }

    private void readEncrypted(int size) throws IOException {
        server.processor.register(client, size * 16, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, ByteChannel channel, byte[] bytes) throws IOException {
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
                        udp.send(data);
                        break;
                    case Constants.TYPE_DNS:
                        dns.lookup(data);
                        break;
                }

                readPacket();
            }
        }, clientDeath);
    }
}

