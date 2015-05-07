package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.client.event.ClientEncryptedPacketLengthListener;
import tk.jackyliao123.proxy.client.event.ClientEncryptedPacketListener;
import tk.jackyliao123.proxy.client.event.ConnectToServerListener;
import tk.jackyliao123.proxy.client.event.TCPListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;

public class Tunnel {
    public final ChannelWrapper serverConnection;
    public final EventProcessor processor;
    public ClientEncryptedPacketLengthListener packetLengthListener;
    public ClientEncryptedPacketListener packetListener;
    public TCPTunnel tcp;
    private AESCipher cipher;

    public Tunnel(EventProcessor processor, byte[] secretKey, TCPListener tcpListener) throws IOException {
        this.processor = processor;
        SocketChannel serverSocketChannel = SocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.connect(Variables.serverAddress);

        this.serverConnection = processor.registerSocketChannel(serverSocketChannel, new ConnectToServerListener(this, secretKey));
        this.packetLengthListener = new ClientEncryptedPacketLengthListener(this);
        this.packetListener = new ClientEncryptedPacketListener(this);

        this.tcp = new TCPTunnel(this, tcpListener);
    }

    public void init(byte[] aesBytes) throws IOException {
        try {
            cipher = new AESCipher(new SecretKeySpec(aesBytes, Constants.AES_ALGORITHM));
            serverConnection.pushFillReadBuffer(ByteBuffer.allocate(1), packetLengthListener);

        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public void onRawData(byte[] data) throws IOException {
        int connectionId;
        byte status;
        int ping;
        int length;
        byte[] packet;
        switch (data[0]) {
            case Constants.TCP_CONNECT:
                connectionId = Util.bs2us(data, 1);
                status = data[3];
                ping = Util.bs2us(data, 4);
                tcp.onConnect(connectionId, status, ping);
                break;
            case Constants.TCP_PACKET:
                connectionId = Util.bs2us(data, 1);
                length = Util.bs2us(data, 3);
                packet = new byte[length];
                System.arraycopy(data, 5, packet, 0, length);
                tcp.onReceive(connectionId, packet);
                break;
            case Constants.TCP_DISCONNECT:
                connectionId = Util.bs2us(data, 1);
                status = data[3];
                tcp.onDisconnect(connectionId, status);
                break;
        }
    }

    public void sendEncryptedPacket(byte[] packet) throws IOException {
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

    public void receiveEncryptedPacket(byte[] packet) throws IOException {
        byte[] data = cipher.decrypt(packet);
        onRawData(data);
    }
}
