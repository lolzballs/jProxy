package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.server.event.EncryptedPacketLengthListener;
import tk.jackyliao123.proxy.server.event.EncryptedPacketListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientConnection {
    public Server server;
    public AESCipher cipher;
    public EncryptedPacketLengthListener packetLengthListener;
    public EncryptedPacketListener packetListener;
    private ChannelWrapper channel;
    private TCPHandler tcp;

    public ClientConnection(Server server, ChannelWrapper channel, AESCipher cipher) {
        this.server = server;
        this.channel = channel;
        this.tcp = new TCPHandler(this);

        this.cipher = cipher;

        this.packetLengthListener = new EncryptedPacketLengthListener(this);
        this.packetListener = new EncryptedPacketListener(this);
    }

    public void sendPacket(byte[] data) throws IOException {
        byte[] enc = cipher.encrypt(data);
        int length = enc.length;
        if (length % 16 != 0) {
            System.err.println("Encryption failed: encrypted data size is not multiple of 16");
            throw new IOException("Encryption failed: encrypted data size is not multiple of 16");
        }
        if (length / 16 >= 256) {
            System.err.println("Error: encrypted blocks is more than 256: " + length / 16);
            throw new IOException("Error: encrypted blocks is more than 256: " + length / 16);
        }
        ByteBuffer b = ByteBuffer.allocate(length + 1);
        b.put((byte) (length / 16));
        b.put(data);
        b.flip();
        channel.pushWriteBuffer(b);
    }

    public void processPacket(byte[] data) {
        System.out.println(Util.bs2str(data));

        switch (data[0]) {
            case Constants.TCP_CONNECT:
                break;
            case Constants.TCP_PACKET:
                break;
            case Constants.TCP_DISCONNECT:
                break;
            case Constants.UDP_ASSOCIATE:
                break;
            case Constants.UDP_PACKET:
                break;
            case Constants.UDP_DISSOCIATE:
                break;
        }
    }
}
