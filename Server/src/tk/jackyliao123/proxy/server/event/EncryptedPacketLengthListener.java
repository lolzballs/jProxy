package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.ClientConnection;
import tk.jackyliao123.proxy.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncryptedPacketLengthListener implements ReadEventListener {
    public final ClientConnection connection;

    public EncryptedPacketLengthListener(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        int length = Util.b2ub(array[0]) * 16;
        channel.pushFillReadBuffer(ByteBuffer.allocate(length), connection.packetListener);
    }
}
