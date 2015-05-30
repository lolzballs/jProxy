package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5MethodLengthListener implements ReadEventListener {
    private final SocksClient client;

    public Socks5MethodLengthListener(SocksClient client) {
        this.client = client;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte version = array[0];
        if (version != Socks5Constants.VERSION) {
            Logger.verbose("Client from " + channel + " connected with not supported Socks version!");
        }
        int methCount = Util.b2ub(array[1]); //In grams
        channel.pushFillReadBuffer(ByteBuffer.allocate(methCount), new Socks5MethodsListener(client));
    }
}
