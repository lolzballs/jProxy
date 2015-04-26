package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.ChannelWrapper;

import java.io.IOException;

public interface DisconnectEventListener {
    void onDisconnect(ChannelWrapper c) throws IOException;
}
