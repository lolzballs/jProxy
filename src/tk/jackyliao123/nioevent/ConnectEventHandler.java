package tk.jackyliao123.nioevent;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public interface ConnectEventHandler {
    void action(AbstractSelectableChannel channel, boolean connectable) throws IOException;
}
