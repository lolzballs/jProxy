package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.server.event.ServerEncryptedPacketLengthListener;
import tk.jackyliao123.proxy.server.event.ServerEncryptedPacketListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class ClientConnection {
    public Server server;
    public AESCipher cipher;
    public ServerEncryptedPacketLengthListener packetLengthListener;
    public ServerEncryptedPacketListener packetListener;
    private ChannelWrapper channel;
    private TCPHandler tcp;

    public ClientConnection(Server server, ChannelWrapper channel, AESCipher cipher) {
        this.server = server;
        this.channel = channel;
        this.tcp = new TCPHandler(this);

        this.cipher = cipher;

        this.packetLengthListener = new ServerEncryptedPacketLengthListener(this);
        this.packetListener = new ServerEncryptedPacketListener(this);
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

        System.out.println("Sending: " + Util.bs2str(data));

        ByteBuffer b = ByteBuffer.allocate(length + 1);
        b.put((byte) (length / 16));
        b.put(enc);
        b.flip();
        channel.pushWriteBuffer(b);
    }

    public SocketAddress getSocketAddress(byte[] b, int offset, int port) throws IOException {
        int addrLen = Util.b2ub(b[offset + 1]);
        byte[] address = new byte[addrLen];
        System.arraycopy(b, offset + 2, address, 0, addrLen);
        switch (b[offset]) {
            case Constants.IPv4:
                if (addrLen != 4) {
                    System.err.println("Invalid length for IPv4 address: " + addrLen);
                }
                return new InetSocketAddress(InetAddress.getByAddress(address), port);
            case Constants.IPv6:
                if (addrLen != 16) {
                    System.err.println("Invalid length for IPv6 address: " + addrLen);
                }
                return new InetSocketAddress(InetAddress.getByAddress(address), port);
            case Constants.DNS:
                return new InetSocketAddress(new String(address, Constants.CHARSET), port);
            default:
                System.err.println("Unknown address type: " + b[offset]);
                throw new IllegalArgumentException("Unknown address type: " + b[offset]);
        }
    }

    public void processPacket(byte[] data) throws IOException {
        System.out.println("Received: " + Util.bs2str(data));

        int connectionId;
        int remotePort;
        int length;
        byte[] buffer;
        switch (data[0]) {
            case Constants.TCP_CONNECT:
                connectionId = Util.bs2us(data, 1);
                remotePort = Util.bs2us(data, 3);
                tcp.connect(connectionId, getSocketAddress(data, 5, remotePort));
                break;
            case Constants.TCP_PACKET:
                connectionId = Util.bs2us(data, 1);
                length = Util.bs2us(data, 3);
                buffer = new byte[length];
                System.arraycopy(data, 5, buffer, 0, length);
                tcp.packet(connectionId, buffer);
                break;
            case Constants.TCP_DISCONNECT:
                connectionId = Util.bs2us(data, 1);
                tcp.disconnect(connectionId);
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
