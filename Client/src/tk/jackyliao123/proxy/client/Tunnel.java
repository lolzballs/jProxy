package tk.jackyliao123.proxy.client;

import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Tunnel {
    private Connection[] connections;

    private final EventProcessor processor;
    private final SocketChannel socket;
    private final Cipher encrypt;
    private final Cipher decrypt;

    public Tunnel(InetSocketAddress address, String username, byte[] password) throws Exception {
        this.connections = new Connection[Constants.MAX_CONNECTIONS];
        this.socket = SocketChannel.open(address);

        // Connect and Authenticate
        SecretKey key = Connector.connect(socket, username, password);
        socket.configureBlocking(false);

        this.encrypt = Cipher.getInstance(Constants.AES_ALGORITHM);
        this.decrypt = Cipher.getInstance(Constants.AES_ALGORITHM);

        encrypt.init(Cipher.ENCRYPT_MODE, key);
        decrypt.init(Cipher.DECRYPT_MODE, key);

        this.processor = new EventProcessor(Selector.open());
    }

    private byte[] encrypt(byte[] data) {
        try {
            return encrypt.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decrypt(byte[] data) {
        try {
            return decrypt.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readPacket() throws IOException {
        processor.register(socket, 1, new ReadEventHandler() {
            @Override
            public void action(EventProcess process, SocketChannel channel, byte[] bytes) throws IOException {
                readEncrypted(bytes[0]);
            }
        });
    }

    private void readEncrypted(int size) throws IOException {
        processor.register(socket, size * 16, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                byte[] data = decrypt(bytes);

                if (data == null) {
                    return;
                }

                System.out.println(Arrays.toString(data));

                byte magic = data[0];
                switch (magic) {
                    case Constants.CONNECTION_TCP:
                        connectionResponse(data);
                        break;
                    case Constants.TYPE_TCP:
                        recieveTCP();
                        break;
                    case Constants.TYPE_UDP:
                        break;
                    case Constants.TYPE_PING:
                        break;
                }

                readPacket();
            }
        });
    }

    private void recieveTCP() {

    }

    private void sendTCP(byte[] data, int id) {

    }

    private void connectTCP(byte[] ip, int port, int id) throws IOException {
        byte[] data = new byte[9];
        data[0] = 0x41;
        data[1] = ip[0];
        data[2] = ip[1];
        data[3] = ip[2];
        data[4] = ip[3];
        data[5] = (byte) (port >> 8);
        data[6] = (byte) (port & 0xFF);
        data[7] = (byte) (id >> 8);
        data[8] = (byte) (id & 0xFF);

        sendEncrypted(socket, data);
    }

    private void connectionResponse(byte[] data) {
        byte status = data[1];
        int id = (data[2] << 8) | data[3];

        switch (status) {
            case Constants.TCP_CONNECTION_OK:
                System.out.println(id + " OK");
                break;
            case Constants.TCP_CONNECTION_TIMEOUT:
                System.out.println(id + " TIMEOUT");
                break;
            case Constants.TCP_CONNECTION_REFUSED:
                System.out.println(id + " REFUSED");
                break;
            case Constants.TCP_CONNECTION_UNREACHABLE:
                System.out.println(id + " UNREACHABLE");
                break;
            case Constants.TCP_CONNECTION_OTHER:
                System.out.println(id + " OTHER");
                break;
        }
    }

    private boolean sendEncrypted(SocketChannel socket, byte[] data) throws IOException {
        byte[] encrypted = encrypt(data);
        if (encrypted == null) {
            return false;
        }

        ByteBuffer send = ByteBuffer.allocate(1 + encrypted.length);
        send.put((byte) (encrypted.length / 16));
        send.put(encrypted);

        send.flip();
        return socket.write(send) == send.capacity();
    }

    public void registerChannel(SocketChannel channel) {
        //TODO SEND SERVER CONNECT PACKET
        //TODO RETRIEVE ID
        //TODO STORE INTO HASHMAP
    }

    public void start() throws IOException {
        connectTCP(new byte[]{127, 0, 0, 1}, 80, 1);

        byte[] payload = "     GET / HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes("UTF-8");
        payload[0] = 1;
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = (byte) ((payload.length - 5) >> 8);
        payload[4] = (byte) ((payload.length - 5) & 0xFF);

        sendEncrypted(socket, payload);

        readPacket();

        while (true) {
            loop();
        }
    }

    private void loop() throws IOException {
        processor.pollEvents();
    }

    public static void main(String[] args) throws Exception {
        Tunnel tunnel = new Tunnel(new InetSocketAddress("localhost", 16384), "lolzballs", "lollolzballs".getBytes("UTF-8"));
        tunnel.start();
    }
}
