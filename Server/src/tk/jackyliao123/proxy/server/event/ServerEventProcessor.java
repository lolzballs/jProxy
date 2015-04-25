package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerEventProcessor extends EventProcessor {
    public ServerEventProcessor() throws IOException {
        super();
    }

    public void registerServerChannel(ServerSocketChannel serverChannel) throws IOException {
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
}
