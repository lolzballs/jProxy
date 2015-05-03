package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.ChannelWrapper;

import java.io.IOException;

public interface ConnectEventListener {
    void onConnect(ChannelWrapper c) throws IOException;
}
