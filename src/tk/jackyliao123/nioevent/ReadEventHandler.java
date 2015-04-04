package tk.jackyliao123.nioevent;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ReadEventHandler {
    void action(EventProcess process, SocketChannel channel, byte[] bytes) throws IOException;
}
