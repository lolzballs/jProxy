package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;

public class Socks5DataListener implements ReadEventListener {
    private final SocksClient client;
    private final int id;

    public Socks5DataListener(SocksClient client, int id) {
        this.client = client;
        this.id = id;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        client.getTCPTunnel().send(id, array);
        channel.pushDumpReadBuffer(this);
    }
}
