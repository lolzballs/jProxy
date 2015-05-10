package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5RequestListener implements ReadEventListener {
	public SocksClient client;

	public Socks5RequestListener(SocksClient client) {
		this.client = client;
	}

	public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte version = array[0];
        if (version != Socks5Constants.VERSION) {
            Logger.error("Client tried using unsupported version " + version + " after connected with supported version!");
        }

        if (array[2] != 0) {
            Logger.warning("Client requested with the RSV field not equal to 0");
        }

        switch (array[2]) {
            case Socks5Constants.ATYP_IPv4:
                channel.pushFillReadBuffer(ByteBuffer.allocate(6), new Socks5AddressListener(client, array[2], array[1]));
                break;
            case Socks5Constants.ATYP_DOMAIN:
				channel.pushFillReadBuffer(ByteBuffer.allocate(1), new Socks5DomainLengthListener(client, array[1]));
                break;
            case Socks5Constants.ATYP_IPv6:
                channel.pushFillReadBuffer(ByteBuffer.allocate(8), new Socks5AddressListener(client, array[2], array[1]));
                break;
            default:
                Logger.warning("Unsupported request AYTP: " + Integer.toHexString(array[2]));
        }
	}
}
