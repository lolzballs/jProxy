package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5DomainLengthListener implements ReadEventListener {
    private final SocksClient client;
    private final byte cmd;

    public Socks5DomainLengthListener(SocksClient client, byte cmd) {
        this.client = client;
        this.cmd = cmd;
    }

    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        channel.pushFillReadBuffer(ByteBuffer.allocate(Util.b2ub(array[0]) + 2), new Socks5AddressListener(client, Socks5Constants.ATYP_DOMAIN, cmd));
    }
}
