package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.client.event.TCPListener;
import tk.jackyliao123.proxy.client.socks.SocksClient;

import java.io.IOException;

public class Socks5TCPListener implements TCPListener {
	private final SocksClient client;

    public Socks5TCPListener(SocksClient client) {
        this.client = client;
    }

	public void onTcpConnect(int connectionId, byte statusCode, int ping) throws IOException {

	}

	public void onTcpPacket(int connectionId, byte[] packet) throws IOException {

	}

	public void onTcpDisconnect(int connectionId, byte reason) throws IOException {

	}
}
