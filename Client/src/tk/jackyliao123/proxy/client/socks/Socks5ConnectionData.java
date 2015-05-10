package tk.jackyliao123.proxy.client.socks;

import tk.jackyliao123.proxy.ChannelWrapper;

public class Socks5ConnectionData {
    public final ChannelWrapper client;
    public final byte atyp;
    public final byte[] addr;
    public final int port;

    public Socks5ConnectionData(ChannelWrapper client, byte atyp, byte[] addr, int port) {
        this.client = client;
        this.atyp = atyp;
        this.addr = addr;
        this.port = port;
    }
}
