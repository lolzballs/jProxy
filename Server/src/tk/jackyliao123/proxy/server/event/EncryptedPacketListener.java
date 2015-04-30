package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.ClientConnection;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncryptedPacketListener implements ReadEventListener {
    public final ClientConnection connection;

    public EncryptedPacketListener(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte[] data = connection.cipher.decrypt(array);
        connection.processPacket(data);

        channel.pushFillReadBuffer(ByteBuffer.allocate(1), connection.packetLengthListener);
    }
}
