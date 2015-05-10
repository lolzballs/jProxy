package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.event.TCPListener;
import tk.jackyliao123.proxy.client.socks.Socks5ConnectionData;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5TCPListener implements TCPListener {
	private final SocksClient client;

    public Socks5TCPListener(SocksClient client) {
        this.client = client;
    }

	public void onTcpConnect(int connectionId, byte statusCode, int ping) throws IOException {
        Socks5ConnectionData c = client.connections[connectionId];

        ByteBuffer response;
        if (c.atyp == Socks5Constants.ATYP_DOMAIN) {
            response = ByteBuffer.allocate(8 + c.addr.length);
        } else {
            response = ByteBuffer.allocate(6 + c.addr.length);
        }

        response.put(Socks5Constants.VERSION);
        switch (statusCode) {
            case Constants.TCP_CONNECTION_GENERAL_FAIL:
                response.put(Socks5Constants.REP_GENERALSOCKSFAILURE);
                break;
            case Constants.TCP_CONNECTION_REFUSED:
                response.put(Socks5Constants.REP_CONNECTIONREFUSED);
                break;
            case Constants.TCP_CONNECTION_TIMEOUT:
                response.put(Socks5Constants.REP_TTLEXPIRED);
                break;
            case Constants.TCP_CONNECTION_UNREACHABLE:
                response.put(Socks5Constants.REP_HOSTUNREACHABLE);
                break;
            case Constants.TCP_CONNECTION_OK:
                response.put(Socks5Constants.REP_SUCCESS);
                break;
            default:
                Logger.error("Unknown connection status: " + Integer.toHexString(statusCode));
                c.client.close();
        }
        response.put((byte) 0);
        response.put(c.atyp);
        if (c.atyp == Socks5Constants.ATYP_DOMAIN) {
            response.put((byte) c.addr.length);
        }
        response.put(c.addr);
        response.put(Util.us2bs(c.port));
        response.flip();

        c.client.pushWriteBuffer(response);
        c.client.pushDumpReadBuffer(new Socks5DataListener(client, connectionId));
	}

	public void onTcpPacket(int connectionId, byte[] packet) throws IOException {
        Socks5ConnectionData c = client.connections[connectionId];
        c.client.pushWriteBuffer(ByteBuffer.wrap(packet));
    }

	public void onTcpDisconnect(int connectionId, byte reason) throws IOException {
        Socks5ConnectionData c = client.connections[connectionId];
        client.connections[connectionId] = null;
        client.freeId(connectionId);
	}
}