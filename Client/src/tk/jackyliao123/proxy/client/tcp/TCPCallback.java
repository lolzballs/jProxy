package tk.jackyliao123.proxy.client.tcp;

public interface TCPCallback {
    void action(TCPConnection connection, byte[] data);
}
