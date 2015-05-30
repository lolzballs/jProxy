package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.DisconnectEventListener;

import java.io.IOException;

public class TunnelDisconnectListener implements DisconnectEventListener {
    private final SocksClient client;

    public TunnelDisconnectListener(SocksClient client) {
        this.client = client;
    }

    @Override
    public void onDisconnect(ChannelWrapper c) throws IOException {
        client.disconnect(c);
        System.err.println("Disconnected");
        System.exit(0);
    }
}
