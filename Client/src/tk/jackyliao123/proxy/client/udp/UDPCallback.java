package tk.jackyliao123.proxy.client.udp;

import tk.jackyliao123.proxy.client.tcp.TCPConnection;

public interface UDPCallback {
    void action(byte[] ip, int port, byte[] data);
}
