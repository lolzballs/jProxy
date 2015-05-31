package tk.jackyliao123.proxy.client.socks;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.TunnelChannelWrapper;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void cleanup() {
        for (Map.Entry<Integer, Socks5ConnectionData> entry : connections.entrySet()) {
            ChannelWrapper wrapper = entry.getValue().client;
            wrapper.disconnectListener = null;
            wrapper.close();
        }
        connections.clear();
    }
}
