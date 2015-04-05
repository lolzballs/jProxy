package tk.jackyliao123.proxy.server.tcp;

import java.nio.channels.SocketChannel;

public class TCPConnection {
    public final int id;
    public final byte[] ip;
    public final int port;
    public final SocketChannel socket;

    public TCPConnection(int id, byte[] ip, int port, SocketChannel socket) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.socket = socket;
    }
}
