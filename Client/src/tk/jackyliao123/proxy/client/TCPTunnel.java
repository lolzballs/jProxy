package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.event.TCPListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TCPTunnel {
    private final Tunnel tunnel;
    private final TCPListener listener;

    public TCPTunnel(Tunnel tunnel, TCPListener listener) {
        this.tunnel = tunnel;
        this.listener = listener;
    }

    public void connect(int connectionId, byte connectionType, byte[] remote, int remotePort) throws IOException {
        if (remote.length >= 256) {
            throw new IOException("Address length too large");
        }
        ByteBuffer buffer = ByteBuffer.allocate(7 + remote.length);
        buffer.put(Constants.TCP_CONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(Util.us2bs(remotePort));
        buffer.put(connectionType);
        buffer.put((byte) remote.length);
        buffer.put(remote);
        buffer.flip();
        tunnel.sendEncryptedPacket(buffer.array());
    }

    public void send(int connectionId, byte[] packet) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(packet.length + 5);
        buffer.put(Constants.TCP_PACKET);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(Util.us2bs(packet.length));
        buffer.put(packet);
        buffer.flip();
        tunnel.sendEncryptedPacket(buffer.array());
    }

    public void disconnect(int connectionId, byte reason) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put(Constants.TCP_DISCONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(reason);
        buffer.flip();
        tunnel.sendEncryptedPacket(buffer.array());
    }

    public void onConnect(int connectionID, byte status, int ping) throws IOException {
        listener.onTcpConnect(connectionID, status, ping);
    }


    public void onReceive(int connectionID, byte[] packet) throws IOException {
        listener.onTcpPacket(connectionID, packet);
    }


    public void onDisconnect(int connectionID, byte reason) throws IOException {
        listener.onTcpDisconnect(connectionID, reason);
    }
}
