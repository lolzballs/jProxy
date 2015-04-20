package tk.jackyliao123.proxy.client;

public interface DNSCallback {
    public void action(int status, String hostname, byte[] ip);
}
