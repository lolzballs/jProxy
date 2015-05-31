package tk.jackyliao123.proxy.client.socks;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.TCPTunnel;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.client.Variables;
import tk.jackyliao123.proxy.client.event.TunnelDisconnectListener;
import tk.jackyliao123.proxy.client.socks.event.Socks5MethodLengthListener;
import tk.jackyliao123.proxy.client.socks.event.Socks5TCPListener;
import tk.jackyliao123.proxy.event.AcceptEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

public class SocksClient implements AcceptEventListener {
    public final EventProcessor processor;
    public final HashMap<Integer, Socks5ConnectionData> connections;
    private final ServerSocketChannel serverChannel;
    private final Tunnel tunnel;
    private boolean connected = false;
    private boolean running = false;

    public SocksClient(int port) throws IOException {
        this.processor = new EventProcessor();
        this.serverChannel = ServerSocketChannel.open();

        this.connections = new HashMap<Integer, Socks5ConnectionData>();

        this.tunnel = new Tunnel(processor, new Socks5TCPListener(this));
        this.tunnel.serverConnection = new Socks5ClientTunnelChannelWrapper(connections, tunnel.rawServerConnection);


        tunnel.serverConnection.disconnectListener = new TunnelDisconnectListener(this);

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));

        processor.registerServerChannel(serverChannel, this);
    }

    public static void main(String[] args) {
        Logger.init(Logger.DEBUG);

        try {
            Variables.loadAllVariables(args);
            Logger.setLoggingLevel(Variables.loggingLevel);

            SocksClient client = new SocksClient(Variables.port);
            client.start();
        } catch (Exception e) {
            Logger.error("SocksClient has experienced a critical error!");
            Logger.error(e);
        }
    }

    public void start() {
        running = true;
        run();
        processor.getSelector().wakeup();

        try {
            serverChannel.close();
        } catch (IOException ignored) {
        }
    }

    public void stop() {
        running = false;
    }

    private void run() {
        while (running) {
            try {
                processor.process(Variables.timeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void freeId(int id) {
        tunnel.freeId(id);
    }

    public int getFreeId() {
        return tunnel.getFreeId();
    }

    public void disconnect(ChannelWrapper c) {
        connected = false;
        System.out.println("Disconnected from " + c.channel);
    }

    @Override
    public void onAccept(ChannelWrapper channel) throws IOException {
        Logger.info("Connected");
        channel.pushFillReadBuffer(ByteBuffer.allocate(2), new Socks5MethodLengthListener(this));
    }

    public TCPTunnel getTCPTunnel() {
        return tunnel.tcp;
    }
}
