package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.DisconnectEventListener;

import java.io.IOException;

public class Socks5DisconnectListener implements DisconnectEventListener {
    public final SocksClient client;
    public final int connectionId;

    public Socks5DisconnectListener(SocksClient client, int connectionId) {
        this.client = client;
        this.connectionId = connectionId;
    }

    public void onDisconnect(ChannelWrapper c) throws IOException {
        client.getTCPTunnel().disconnect(connectionId, Constants.TCP_DISCONNECT_CONNECTION_RESET);
    }
}
