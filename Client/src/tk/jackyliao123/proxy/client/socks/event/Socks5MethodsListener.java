package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5MethodsListener implements ReadEventListener {
    private final SocksClient client;

    public Socks5MethodsListener(SocksClient client) {
        this.client = client;
    }

    public void sendResponse(ChannelWrapper channel, byte method) {
        ByteBuffer bytes = ByteBuffer.allocate(2);
        bytes.put(Socks5Constants.VERSION);
        bytes.put(method);
        bytes.flip();

        channel.pushWriteBuffer(bytes);
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        for (byte method : array) {
            if (method == Socks5Constants.AUTHMETHOD_NONE) {
                sendResponse(channel, method);

                Logger.info("Client from " + channel.channel + " connected.");
                channel.pushFillReadBuffer(ByteBuffer.allocate(4), new Socks5RequestListener(client));
                return;
            }
        }

        sendResponse(channel, Socks5Constants.AUTHMETHOD_NOACCEPTABLE);
        Logger.verbose("Client from " + channel.channel + " did not have an acceptable authentication method. ");
    }
}
