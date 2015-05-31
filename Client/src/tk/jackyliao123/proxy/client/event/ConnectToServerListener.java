package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.event.ConnectEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectToServerListener implements ConnectEventListener {
    private final Tunnel tunnel;

    public ConnectToServerListener(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    @Override
    public boolean onConnect(ChannelWrapper c) throws IOException {
        SocketChannel channel = (SocketChannel) c.channel;
        boolean connected = channel.finishConnect();
        if (!connected) {
            throw new IOException("Connection Failed");
        }
        ByteBuffer handshake = ByteBuffer.allocate(Constants.MAGIC_LENGTH + 2);
        handshake.put(Constants.MAGIC);
        handshake.put(Constants.MAJOR);
        handshake.put(Constants.MINOR);
        handshake.flip();
        c.pushWriteBuffer(handshake);
        c.pushFillReadBuffer(ByteBuffer.allocate(Constants.MAGIC_LENGTH + 1), new HandshakeResponseListener(tunnel));
        return true;
    }
}
