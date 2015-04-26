package tk.jackyliao123.proxy.event;

import tk.jackyliao123.proxy.ChannelWrapper;

import java.io.IOException;

public interface ReadEventListener {
    void onRead(ChannelWrapper channel, byte[] array) throws IOException;
}
