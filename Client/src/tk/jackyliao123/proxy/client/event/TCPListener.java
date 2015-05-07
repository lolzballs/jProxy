package tk.jackyliao123.proxy.client.event;

public interface TCPListener {
    void onTcpConnect(int connectionId, byte statusCode, int ping);

    void onTcpPacket(int connectionId, byte[] packet);

    void onTcpDisconnect(int connectionId, byte reason);
}
