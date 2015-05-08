package tk.jackyliao123.proxy.client.event;

import java.io.IOException;

public interface TCPListener {
    void onTcpConnect(int connectionId, byte statusCode, int ping) throws IOException;

    void onTcpPacket(int connectionId, byte[] packet) throws IOException;

    void onTcpDisconnect(int connectionId, byte reason) throws IOException;
}
