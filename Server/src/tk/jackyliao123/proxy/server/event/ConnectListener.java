package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.event.ConnectEventListener;
import tk.jackyliao123.proxy.server.TCPHandler;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class ConnectListener implements ConnectEventListener {
    public final int connectionId;
    private final TCPHandler handler;

    public ConnectListener(TCPHandler handler, int connectionId) {
        this.connectionId = connectionId;
        this.handler = handler;
    }

    @Override
    public boolean onConnect(ChannelWrapper channel) throws IOException {
        if (channel.channel instanceof SocketChannel) {
            int timeTaken = (int) (System.currentTimeMillis() - channel.currentTimestamp);
            try {
                SocketChannel sChannel = (SocketChannel) channel.channel;
                boolean isConnected = sChannel.finishConnect();
                if (isConnected) {
                    handler.sendConnect(connectionId, Constants.TCP_CONNECTION_OK, timeTaken);
                    channel.pushDumpReadBuffer(new RedirectToClientListener(handler, connectionId));
                    channel.disconnectListener = new RedirectDisconnectListener(handler, connectionId);
                    return true;
                } else {
                    handler.sendConnect(connectionId, Constants.TCP_CONNECTION_GENERAL_FAIL, timeTaken);
                    handler.tcpConnections.remove(connectionId);
                }
            } catch (SocketException e) {
                String message = e.getMessage();
                byte status;
                if (message.equals("Network is unreachable")) {
                    status = Constants.TCP_CONNECTION_UNREACHABLE;
                } else if (message.equals("Connection refused")) {
                    status = Constants.TCP_CONNECTION_REFUSED;
                } else if (message.equals("Connection timed out")) {
                    status = Constants.TCP_CONNECTION_TIMEOUT;
                } else {
                    Logger.warning("Unknown TCP connection error: " + e.getMessage());
                    status = Constants.TCP_CONNECTION_GENERAL_FAIL;
                }
                handler.sendConnect(connectionId, status, timeTaken);
                handler.tcpConnections.remove(connectionId);
                Logger.error(e);
            } catch (IOException e) {
                handler.sendConnect(connectionId, Constants.TCP_CONNECTION_GENERAL_FAIL, timeTaken);
                handler.tcpConnections.remove(connectionId);
                Logger.error(e);
            }
        }
        return false;
    }
}
