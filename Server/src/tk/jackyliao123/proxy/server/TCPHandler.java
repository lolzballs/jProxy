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

public class TCPHandler {
    public ChannelWrapper[] tcpConnections;
    private ClientConnection client;

    public TCPHandler(ClientConnection client) {
        this.client = client;

        this.tcpConnections = new ChannelWrapper[Constants.MAX_CONNECTIONS];
    }

    public void sendConnect(int connectionId, byte responseCode, int ping) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(Constants.TCP_CONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(responseCode);
        buffer.put(Util.us2bs(ping));
        buffer.flip();
        client.sendPacket(buffer.array());
    }

    public void sendDisconnect(int connectionId, byte reasonCode) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(Constants.TCP_DISCONNECT);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(reasonCode);
        buffer.flip();
        client.sendPacket(buffer.array());
    }

    public void sendPacket(int connectionId, byte[] data) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
        buffer.put(Constants.TCP_PACKET);
        buffer.put(Util.us2bs(connectionId));
        buffer.put(Util.us2bs(data.length));
        buffer.put(data);
        buffer.flip();
        client.sendPacket(buffer.array());
    }

    public void connect(int connectionId, SocketAddress addr) throws IOException {
        Logger.verbose("Connect: " + addr);
        if (tcpConnections[connectionId] != null) {
            tcpConnections[connectionId].disconnectListener = null;
            tcpConnections[connectionId].closeOnFinishData();
        }
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(addr);
            tcpConnections[connectionId] = client.server.processor.registerSocketChannel(channel, new ConnectListener(this, connectionId));
        } catch (IOException e) {
            sendConnect(connectionId, Constants.TCP_CONNECTION_GENERAL_FAIL, 65535);
        }
    }

    public void packet(int connectionId, byte[] packet) throws IOException {
        if (tcpConnections[connectionId] != null) {
                ByteBuffer b = ByteBuffer.allocate(packet.length);
                b.put(packet);
                b.flip();
                tcpConnections[connectionId].pushWriteBuffer(b);
        }
    }

    public void disconnect(int connectionId, byte reason) throws IOException {
        closeConnection(connectionId, true);
        sendDisconnect(connectionId, reason);
    }

    public void closeConnection(int connectionId, boolean event) throws IOException {
        if (tcpConnections[connectionId] != null) {
            if(!event){
                tcpConnections[connectionId].disconnectListener = null;
            }
            tcpConnections[connectionId].closeOnFinishData();
        }
        tcpConnections[connectionId] = null;
    }
}
