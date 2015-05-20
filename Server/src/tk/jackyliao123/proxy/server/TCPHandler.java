package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.server.event.ConnectListener;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class TCPHandler {
    public HashMap<Integer, ChannelWrapper> tcpConnections;
    private ClientConnection client;

    public TCPHandler(ClientConnection client) {
        this.client = client;

        this.tcpConnections = new HashMap<Integer, ChannelWrapper>();
    }

    public void sendConnect(int connectionId, byte responseCode, int ping) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(Constants.TCP_CONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(responseCode);
        buffer.put(Util.us2bs(ping));
        buffer.flip();
        client.sendPacket(connectionId, buffer.array());
    }

    public void sendDisconnect(int connectionId, byte reasonCode) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(Constants.TCP_DISCONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(reasonCode);
        buffer.flip();
        client.sendPacket(connectionId, buffer.array());
    }

    public void sendPacket(int connectionId, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
        buffer.put(Constants.TCP_PACKET);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(Util.us2bs(data.length));
        buffer.put(data);
        buffer.flip();
        client.sendPacket(connectionId, buffer.array());
    }

    public void connect(int connectionId, SocketAddress addr) throws IOException {
        Logger.verbose("Connect: " + addr);
        ChannelWrapper prevConnection = tcpConnections.get(connectionId);
        if (prevConnection != null) {
            Logger.warning("Connection id " + connectionId + " is previously connected. Disconnecting previous connection.");
            prevConnection.disconnectListener = null;
            prevConnection.closeOnFinishData();
        }
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(addr);
            if (tcpConnections.size() >= Constants.MAX_CONNECTIONS) {
                sendConnect(connectionId, Constants.TCP_CONNECTION_LIMIT_EXCEED, 65535);
                return;
            }
            tcpConnections.put(connectionId, client.server.processor.registerSocketChannel(channel, new ConnectListener(this, connectionId)));
        } catch (IOException e) {
            sendConnect(connectionId, Constants.TCP_CONNECTION_GENERAL_FAIL, 65535);
        }
    }

    public void packet(int connectionId, byte[] packet) throws IOException {
        ChannelWrapper channel = tcpConnections.get(connectionId);
        if (channel == null) {
            Logger.error("Connection id " + connectionId + " is null on packet");
            return;
        }
        ByteBuffer b = ByteBuffer.allocate(packet.length);
        b.put(packet);
        b.flip();
        channel.pushWriteBuffer(b);
    }

    public void disconnect(int connectionId, byte reason) throws IOException {
        if (tcpConnections.get(connectionId) == null) {
            Logger.error("Connection id " + connectionId + " is null on disconnect");
            return;
        }
        closeWithoutListener(connectionId);
    }

    public void closeWithoutListener(int connectionId) {
        ChannelWrapper wrapper = tcpConnections.remove(connectionId);
        if (wrapper != null) {
            wrapper.disconnectListener = null;
            wrapper.closeOnFinishData();
        }
    }

    public void closeConnection(int connectionId) throws IOException {
        ChannelWrapper wrapper = tcpConnections.remove(connectionId);
        if (wrapper != null) {
            wrapper.closeOnFinishData();
        }
    }
}
