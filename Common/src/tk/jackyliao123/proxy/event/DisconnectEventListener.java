package tk.jackyliao123.proxy.event;

import java.nio.channels.SocketChannel;

public interface DisconnectEventListener {
    void onDisconnect(SocketChannel c);
}
