package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.TunnelChannelWrapper;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ServerTunnelChannelWrapper extends TunnelChannelWrapper {
    public HashMap<Integer, ChannelWrapper> connections;

    public ServerTunnelChannelWrapper(ChannelWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void stopReading(int connectionId) {
        ChannelWrapper connection = connections.get(connectionId);
        if (connection == null) {
            Logger.warning("Connection id " + connectionId + " is null on stopReading");
            return;
        }
        connection.removeInterest(SelectionKey.OP_READ);
        connection.stopReading = true;
    }

    @Override
    public void startReading(int connectionId) {
        ChannelWrapper connection = connections.get(connectionId);
        if (connection == null) {
            Logger.warning("Connection id " + connectionId + " is null on startReading");
            return;
        }
        connection.addInterest(SelectionKey.OP_READ);
        connection.stopReading = false;
    }

    @Override
    public void cleanup() {
        for (Map.Entry<Integer, ChannelWrapper> entry : connections.entrySet()) {
            ChannelWrapper wrapper = entry.getValue();
            wrapper.disconnectListener = null;
            wrapper.close();
        }
        connections.clear();
    }
}
