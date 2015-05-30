package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.TCPHandler;

import java.io.IOException;

public class RedirectToClientListener implements ReadEventListener {
    private final TCPHandler tcp;
    private final int connectionId;

    public RedirectToClientListener(TCPHandler tcp, int connectionId) {
        this.tcp = tcp;
        this.connectionId = connectionId;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        tcp.sendPacket(connectionId, array);
        channel.pushDumpReadBuffer(this);
    }
}
