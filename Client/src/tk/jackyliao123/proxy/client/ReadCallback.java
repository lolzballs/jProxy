package tk.jackyliao123.proxy.client;

public interface ReadCallback {
    public void action(TCPConnection connection, byte[] data);
}
