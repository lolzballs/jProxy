package tk.jackyliao123.proxy.client.socks;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.TCPTunnel;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.client.Variables;
import tk.jackyliao123.proxy.client.socks.event.Socks5MethodLengthListener;
import tk.jackyliao123.proxy.client.socks.event.Socks5TCPListener;
import tk.jackyliao123.proxy.client.socks.event.TunnelDisconnectListener;
import tk.jackyliao123.proxy.event.AcceptEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayDeque;

public class SocksClient implements AcceptEventListener {
    public final EventProcessor processor;
    private final ServerSocketChannel serverChannel;
    private final Tunnel tunnel;
    private boolean connected = false;
    private boolean running = false;

    public final Socks5ConnectionData[] connections;
    private final ArrayDeque<Integer> freeIds;

    public SocksClient(int port, byte[] key) throws IOException {
        this.processor = new EventProcessor();
        this.serverChannel = ServerSocketChannel.open();
        this.tunnel = new Tunnel(processor, key, new Socks5TCPListener(this));

        this.connections = new Socks5ConnectionData[Constants.MAX_CONNECTIONS];
        this.freeIds = new ArrayDeque<Integer>();

        tunnel.serverConnection.disconnectListener = new TunnelDisconnectListener(this);

        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        processor.registerServerChannel(serverChannel, this);
        for (int i = Constants.MAX_CONNECTIONS - 1; i >= 0; --i) {
            freeIds.push(i);
        }
    }

    public int getFreeId() {
        if (!freeIds.isEmpty()) {
            return freeIds.pop();
        }
        Logger.error("Out of ids. This can cause severe errors");
        return -1;
    }

    public void freeId(int id){
        freeIds.push(id);
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

    public void disconnect(ChannelWrapper c) {
        connected = false;
        System.out.println("Disconnected from " + c.channel);
    }

    @Override
    public void onAccept(ChannelWrapper channel) throws IOException {
        channel.pushFillReadBuffer(ByteBuffer.allocate(2), new Socks5MethodLengthListener(this));
    }

    public TCPTunnel getTCPTunnel() {
        return tunnel.tcp;
    }

    public static void main(String[] args) {
        Logger.init(Logger.DEBUG);

        try {
            DataInputStream input = new DataInputStream(new FileInputStream(new File("keys.dat")));
            input.readUTF();
            byte[] key = new byte[Constants.SECRET_SALT_SIZE];
            input.readFully(key);

            Variables.loadAllVariables(args);
            Logger.setLoggingLevel(Variables.loggingLevel);

            SocksClient client = new SocksClient(Variables.port, key);
            client.start();
        } catch (Exception e) {
            Logger.error("SocksClient has experienced a critical error!");
            Logger.error(e);
        }
    }
}
