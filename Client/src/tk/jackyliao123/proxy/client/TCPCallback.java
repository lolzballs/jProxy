package tk.jackyliao123.proxy.client;

public interface TCPCallback {
    public void action(TCPConnection connection, byte[] data);
}
