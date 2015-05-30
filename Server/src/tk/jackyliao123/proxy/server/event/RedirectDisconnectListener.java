package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.event.DisconnectEventListener;
import tk.jackyliao123.proxy.server.TCPHandler;

import java.io.IOException;

public class RedirectDisconnectListener implements DisconnectEventListener {
    public final TCPHandler handler;
    public final int connectionId;

    public RedirectDisconnectListener(TCPHandler handler, int connectionId) {
        this.handler = handler;
        this.connectionId = connectionId;
    }

    @Override
    public void onDisconnect(ChannelWrapper c) throws IOException {
        handler.closeConnection(connectionId);
        handler.sendDisconnect(connectionId, Constants.TCP_DISCONNECT_CONNECTION_RESET);
    }
}
