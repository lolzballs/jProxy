package tk.jackyliao123.proxy.client;

import tk.jackyliao123.nioevent.DeathEventHandler;
import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.AESCipher;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.tcp.TCPCallback;
import tk.jackyliao123.proxy.client.tcp.TCPTunnel;
import tk.jackyliao123.proxy.client.udp.UDPCallback;
import tk.jackyliao123.proxy.client.udp.UDPTunnel;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Tunnel {
    public final SocketChannel server;
    private final EventProcessor processor;
    private final AESCipher cipher;
    private final DeathEventHandler death;

    public final TCPTunnel tcp;
    public final UDPTunnel udp;
    public final DNSTunnel dns;

    public Tunnel(InetSocketAddress address, String username, byte[] password, TCPCallback tcpcallback, UDPCallback udpcallback) throws Exception {
        this.server = SocketChannel.open(address);

        // Connect and Authenticate
        SecretKey key = Connector.connect(server, username, password);
        server.configureBlocking(false);

        this.cipher = new AESCipher(key);
        this.processor = new EventProcessor(Selector.open());
        this.death = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                System.out.println("Disconnected from server.");
                System.exit(0);
            }
        };

        this.tcp = new TCPTunnel(this, tcpcallback);
        this.udp = new UDPTunnel(this, udpcallback);
        this.dns = new DNSTunnel(this);
    }

    public synchronized boolean sendEncrypted(byte[] data) throws IOException {
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
            written += server.write(send);
        }
        return written == send.capacity();
    }

    private void readPacket() throws IOException {
        processor.register(server, 1, new ReadEventHandler() {
            @Override
            public void action(EventProcess process, ByteChannel channel, byte[] bytes) throws IOException {
                readEncrypted(bytes[0]);
            }
        }, death);
    }

    private void readEncrypted(int size) throws IOException {
        System.out.println(size * 16);
        processor.register(server, size * 16, new ReadEventHandler() {
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
                    case Constants.DISCONNECT_TCP:
                        tcp.disconnect(data);
                        break;
                    case Constants.TYPE_TCP:
                        tcp.read(data);
                        break;
                    case Constants.TYPE_UDP:
                        udp.read(data);
                        break;
                    case Constants.TYPE_PING:
                        break;
                    case Constants.TYPE_DNS:
                        dns.found(data);
                        break;
                }

                readPacket();
            }
        }, death);
    }

    public void registerChannel(SocketChannel channel) {
        //TODO SEND SERVER CONNECT PACKET
        //TODO RETRIEVE ID
        //TODO STORE INTO HASHMAP
    }

    public void start() throws IOException {
        readPacket();

        while (true) {
            loop();
        }
    }

    private void loop() throws IOException {
        processor.pollEvents();
    }

//    public static void main(String[] args) throws Exception {
//        Tunnel tunnel = new Tunnel(new InetSocketAddress("localhost", 16384), "lolzballs", "lollolzballs".getBytes("UTF-8"));
//        tunnel.start();
//    }
}
