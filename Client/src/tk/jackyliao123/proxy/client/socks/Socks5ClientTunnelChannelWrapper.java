package tk.jackyliao123.proxy.client.socks;

import com.sun.org.apache.bcel.internal.generic.Select;
import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.TunnelChannelWrapper;
import tk.jackyliao123.proxy.client.socks.Socks5ConnectionData;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Socks5ClientTunnelChannelWrapper extends TunnelChannelWrapper {
    public final HashMap<Integer, Socks5ConnectionData> connections;

    public Socks5ClientTunnelChannelWrapper(HashMap<Integer, Socks5ConnectionData> connections, ChannelWrapper wrapper) {
        super(wrapper);
        this.connections = connections;
    }

    @Override
    public void stopReading(int connectionId) {
        Socks5ConnectionData connection = connections.get(connectionId);
        if (connection == null) {
            Logger.warning("Connection id " + connectionId + " is null on stopReading");
            return;
        }
        connection.client.removeInterest(SelectionKey.OP_READ);
        connection.client.stopReading = true;
    }

    @Override
    public void startReading(int connectionId) {
        Socks5ConnectionData connection = connections.get(connectionId);
        if (connection == null) {
            Logger.warning("Connection id " + connectionId + " is null on startReading");
            return;
        }
        connection.client.addInterest(SelectionKey.OP_READ);
        connection.client.stopReading = false;
    }
}
