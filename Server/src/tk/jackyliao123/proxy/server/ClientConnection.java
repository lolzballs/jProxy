package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.TunnelChannelWrapper;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.server.event.ServerEncryptedPacketLengthListener;
import tk.jackyliao123.proxy.server.event.ServerEncryptedPacketListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.UnresolvedAddressException;

public class ClientConnection {
    public Server server;
    public AESCipher cipher;
    public ServerEncryptedPacketLengthListener packetLengthListener;
    public ServerEncryptedPacketListener packetListener;
    private TunnelChannelWrapper channel;
    private TCPHandler tcp;

    public ClientConnection(Server server, TunnelChannelWrapper channel, AESCipher cipher) {
        this.server = server;
        this.channel = channel;
        this.tcp = new TCPHandler(this);

        this.cipher = cipher;

        this.packetLengthListener = new ServerEncryptedPacketLengthListener(this);
        this.packetListener = new ServerEncryptedPacketListener(this);
    }

    public void sendPacket(int id, byte[] data) throws IOException {
        byte[] enc = cipher.encrypt(data);
        int length = enc.length;
        if (length % 16 != 0) {
            throw new IOException("Encryption failed: encrypted data size is not multiple of 16");
        }
        if (length / 16 >= 256) {
            throw new IOException("Error: encrypted blocks is more than 256: " + length / 16);
        }

        Logger.debug("Sending: " + Util.bs2str(data));

        ByteBuffer b = ByteBuffer.allocate(length + 1);
        b.put((byte) (length / 16));
        b.put(enc);
        b.flip();
        channel.pushWriteBuffer(id, b);
    }

    public SocketAddress getSocketAddress(byte[] b, int offset, int port) throws IOException {
        int addrLen = Util.b2ub(b[offset + 1]);
        byte[] address = new byte[addrLen];
        System.arraycopy(b, offset + 2, address, 0, addrLen);
        switch (b[offset]) {
            case Constants.IPv4:
                if (addrLen != 4) {
                    Logger.error("Invalid length for IPv4 address: " + addrLen);
                }
                return new InetSocketAddress(InetAddress.getByAddress(address), port);
            case Constants.IPv6:
                if (addrLen != 16) {
                    Logger.error("Invalid length for IPv6 address: " + addrLen);
                }
                return new InetSocketAddress(InetAddress.getByAddress(address), port);
            case Constants.DNS:
                return new InetSocketAddress(new String(address, Constants.CHARSET), port);
            default:
                throw new IllegalArgumentException("Unknown address type: " + b[offset]);
        }
    }

    public void processPacket(byte[] data) throws IOException {
        Logger.debug("Received: " + Util.bs2str(data));

        int connectionId;
        int remotePort;
        int length;
        byte[] buffer;
        byte reason;

        try {
            switch (data[0]) {
                case Constants.TCP_CONNECT:
                    connectionId = Util.bs2us(data, 1);
                    remotePort = Util.bs2us(data, 3);
                    try {
                        tcp.connect(connectionId, getSocketAddress(data, 5, remotePort));
                    } catch (UnresolvedAddressException e) {
                        tcp.sendConnect(connectionId, Constants.TCP_CONNECTION_UNKNOWN_HOST, 65535);
                        Logger.warning("Unresolved address received");
                    }
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
                    reason = data[3];
                    tcp.disconnect(connectionId, reason);
                    break;
                case Constants.UDP_ASSOCIATE:
                    //TODO IMPLEMENT
                    break;
                case Constants.UDP_PACKET:
                    //TODO IMPLEMENT
                    break;
                case Constants.UDP_DISSOCIATE:
                    //TODO IMPLEMENT
                    break;
            }
        } catch (CancelledKeyException e) {
            Logger.warning("Cancelled key whilst processing incoming packet");
        } catch (Exception e) {
            Logger.error("Error occurred");
            Logger.error(e);
        }
    }
}
