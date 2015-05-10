package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5MethodLengthListener implements ReadEventListener {
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte version = array[0];
        if (version != Socks5Constants.VERSION) {
            Logger.verbose("Client from " + channel + " connected with not-supported Socks version!");
        }
        byte methCount = array[1];
        channel.pushFillReadBuffer(ByteBuffer.allocate(methCount), new Socks5MethodsListener());
    }
}
