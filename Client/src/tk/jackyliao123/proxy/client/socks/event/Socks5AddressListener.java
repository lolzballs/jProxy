package tk.jackyliao123.proxy.client.socks.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.socks.Socks5ConnectionData;
import tk.jackyliao123.proxy.client.socks.Socks5Constants;
import tk.jackyliao123.proxy.client.socks.SocksClient;
import tk.jackyliao123.proxy.event.ReadEventListener;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Socks5AddressListener implements ReadEventListener {
    private final SocksClient client;
    private final byte atyp;
    private final byte cmd;

    public Socks5AddressListener(SocksClient client, byte atyp, byte cmd) {
        this.client = client;
        this.atyp = atyp;
        this.cmd = cmd;
    }

    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte type;
        byte[] addr;
        int port;

        switch (atyp) {
            case Socks5Constants.ATYP_IPv4:
                type = Constants.IPv4;
                addr = new byte[4];
                System.arraycopy(array, 0, addr, 0, 4);
                port = Util.bs2us(array, 4);
                break;
            case Socks5Constants.ATYP_DOMAIN:
                type = Constants.DNS;
                addr = new byte[array.length - 2];
                System.arraycopy(array, 0, addr, 0, array.length - 2);
                port = Util.bs2us(array, array.length - 2);
                break;
            case Socks5Constants.ATYP_IPv6:
                type = Constants.IPv6;
                addr = new byte[16];
                System.arraycopy(array, 0, addr, 0, 16);
                port = Util.bs2us(array, 16);
                break;
            default:
                type = -1;
                addr = null;
                port = -1;
                Logger.error("Unknown ATYP, killing connection!");
                channel.close();
        }

        switch (cmd) {
            case Socks5Constants.CMD_CONNECT:
                int id = client.getFreeId();
                client.connections[id] = new Socks5ConnectionData(channel, atyp, addr, port);
                client.getTCPTunnel().connect(id, type, addr, port);
                break;
            case Socks5Constants.CMD_BIND:
                // TODO: IMPLEMENT
                break;
            case Socks5Constants.CMD_UDPASSOCIATE:
                // TODO: IMPLEMENT
                break;
            default:
                Logger.warning("Unsupported request command: " + Integer.toHexString(array[1]));
        }
    }
}
