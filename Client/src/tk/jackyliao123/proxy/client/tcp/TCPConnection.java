package tk.jackyliao123.proxy.client.tcp;

import tk.jackyliao123.proxy.Constants;

public class TCPConnection {
    public final int id;
    public final byte[] ip;
    public final int port;
    public int status;

    public TCPConnection(int id, byte[] ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.status = Constants.TCP_CONNECTION_OTHER;
    }
}
