package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.DisconnectEventListener;

import java.io.IOException;

public class Socks5DisconnectListener implements DisconnectEventListener {
    public final SocksClient client;

    public Socks5DisconnectListener(SocksClient client) {
        this.client = client;
    }

    public void onDisconnect(ChannelWrapper c) throws IOException {
        //TODO IMPLEMENT DISCONNECT
    }
}
