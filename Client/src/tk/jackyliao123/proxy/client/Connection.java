package tk.jackyliao123.proxy.client;

public class Connection {
    public final int id;
    public final byte[] ip;
    public final int port;

    public Connection(int id, byte[] ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }
}
