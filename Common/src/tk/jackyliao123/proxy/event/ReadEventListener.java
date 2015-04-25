package tk.jackyliao123.proxy.event;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;

public interface ReadEventListener {
    void onRead(AbstractSelectableChannel channel, byte[] array, Object extra) throws IOException;
}
