package tk.jackyliao123.nioevent;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public interface ReadEventHandler {
    void action(EventProcess process, ByteChannel channel, byte[] bytes) throws IOException;
}
