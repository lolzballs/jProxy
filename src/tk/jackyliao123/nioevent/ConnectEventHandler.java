package tk.jackyliao123.nioevent;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ConnectEventHandler {
    void action(SocketChannel channel, boolean connectable) throws IOException;
}
