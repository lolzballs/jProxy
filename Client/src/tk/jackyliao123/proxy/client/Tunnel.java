package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.client.event.ConnectToServerListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.*;

public class Tunnel {
    private final ChannelWrapper serverConnection;
    private final EventProcessor processor;
    private final byte[] secretKey;
    private AESCipher cipher;

    public Tunnel(EventProcessor processor, byte[] secretKey) throws IOException {
        this.secretKey = secretKey;
        this.processor = processor;
        SocketChannel serverSocketChannel = SocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.connect(Variables.serverAddress);
        serverConnection = processor.registerSocketChannel(serverSocketChannel, new ConnectToServerListener(this, secretKey));
    }

    public void init(byte[] aesBytes) throws IOException {
        try {
            cipher = new AESCipher(new SecretKeySpec(aesBytes, Constants.AES_ALGORITHM));

        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public void connect(int connectionId, String remoteAddr) {
    }

    public void receivePacket(byte[] packet) throws IOException {

    }

    public void sendPacket(byte[] packet) throws IOException {
        byte[] encrypted = cipher.encrypt(packet);
        if (encrypted.length % 16 != 0) {
            System.err.println("Encrypted packet size is not multiple of 16" + encrypted.length);
            throw new IOException("Encrypted packet size is not multiple of 16: " + encrypted.length);
        }
        int length = encrypted.length / 16;
        if (length >= 256) {
            System.err.println("Encrypted number of blocks is greater than 255: " + length);
            throw new IOException("Encrypted number of blocks is greater than 255: " + length);
        }
        ByteBuffer buffer = ByteBuffer.allocate(packet.length + 1);
        buffer.put((byte) length);
        buffer.put(encrypted);
        buffer.flip();
    }
}
