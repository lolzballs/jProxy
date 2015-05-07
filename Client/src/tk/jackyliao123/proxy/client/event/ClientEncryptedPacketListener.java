package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientEncryptedPacketListener implements ReadEventListener {

    private final Tunnel tunnel;

    public ClientEncryptedPacketListener(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        tunnel.receiveEncryptedPacket(array);
        channel.pushFillReadBuffer(ByteBuffer.allocate(1), tunnel.packetLengthListener);
    }
}
