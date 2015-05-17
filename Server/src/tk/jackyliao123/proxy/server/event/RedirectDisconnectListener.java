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

    public void onDisconnect(ChannelWrapper c) throws IOException {
        handler.disconnect(connectionId, Constants.TCP_DISCONNECT_BROKEN_PIPE);
    }
}
