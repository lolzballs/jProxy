package tk.jackyliao123.proxy.server.event;

import java.nio.channels.SocketChannel;

public interface AcceptEventListener {
    void onAccept(SocketChannel channel);
}
