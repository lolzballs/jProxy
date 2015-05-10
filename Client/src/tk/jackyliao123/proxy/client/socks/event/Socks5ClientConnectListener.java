package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.event.ConnectEventListener;

import java.io.IOException;

public class Socks5ClientConnectListener implements ConnectEventListener {
	public boolean onConnect(ChannelWrapper c) throws IOException
		return false;
	}
}
