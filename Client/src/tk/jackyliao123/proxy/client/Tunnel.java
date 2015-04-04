package tk.jackyliao123.proxy.client;

import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashMap;

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
                        connectTCP(data);
                        break;
                    case Constants.TYPE_TCP:
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

    private void connectTCP(byte[] data) {
        byte status = data[1];
        int id = (data[2] << 8) | data[3];

        switch (status) {

        }
    }

    private boolean sendEncrypted(SocketChannel socket, byte[] data) throws IOException {
        byte[] encrpyted = encrypt(data);
        if (encrpyted == null) {
            return false;
        }

        ByteBuffer send = ByteBuffer.allocate(1 + encrpyted.length);
        send.put((byte) (encrpyted.length / 16));
        send.put(encrpyted);

        send.flip();
        return socket.write(send) == send.capacity();
    }

    public void registerChannel(SocketChannel channel) {
        //TODO SEND SERVER CONNECT PACKET
        //TODO RETRIEVE ID
        //TODO STORE INTO HASHMAP
    }

    public void start() throws IOException {
        byte[] data = new byte[9];
        data[0] = 0x41;
        data[1] = (byte) 127;
        data[2] = (byte) 0;
        data[3] = (byte) 0;
        data[4] = (byte) 1;
        data[5] = 0;
        data[6] = 80;
        data[7] = 0;
        data[8] = 1;

        sendEncrypted(socket, data);

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
