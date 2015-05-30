package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientEncryptedPacketLengthListener implements ReadEventListener {
    private final Tunnel tunnel;

    public ClientEncryptedPacketLengthListener(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        int length = Util.b2ub(array[0]) * 16;
        channel.pushFillReadBuffer(ByteBuffer.allocate(length), tunnel.packetListener);
    }
}
