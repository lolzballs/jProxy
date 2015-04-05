package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;

public class Connection {
    public final int id;
    public final byte[] ip;
    public final int port;
    public int status;

    public Connection(int id, byte[] ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.status = Constants.TCP_CONNECTION_OTHER;
    }
}
