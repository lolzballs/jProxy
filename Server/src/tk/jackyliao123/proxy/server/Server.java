package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.event.AcceptEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;
import tk.jackyliao123.proxy.server.event.AuthenticateListener;
import tk.jackyliao123.proxy.server.event.HandshakeListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

public class Server implements AcceptEventListener {
    public EventProcessor processor;
    public HashMap<String, ClientConnection> connections;
    public HandshakeListener handshakeListener;
    public AuthenticateListener authenticateListener;
    private ServerSocketChannel serverChannel;
    private boolean running;

    public Server(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.processor = new EventProcessor();

        this.connections = new HashMap<String, ClientConnection>();

        this.handshakeListener = new HandshakeListener(this);
        this.authenticateListener = new AuthenticateListener(this);

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        processor.registerServerChannel(serverChannel, this);
    }

    public static void main(String[] args) {
        Logger.init(Logger.DEBUG);

        try {
            Variables.loadAllVariables(args);
            Logger.setLoggingLevel(Variables.loggingLevel);

            Server server = new Server(Variables.serverPort);
            server.start();
        } catch (Exception e) {
            Logger.error("Server has experienced a critical error");
            Logger.error(e);
        }
    }

    @Override
    public void onAccept(ChannelWrapper channel) throws IOException {
        channel.pushFillReadBuffer(ByteBuffer.allocate(Constants.MAGIC_LENGTH + 2), handshakeListener);
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
        processor.getSelector().wakeup();

        try {
            serverChannel.close();
        } catch (IOException ignored) {
        }
    }

    private void run() {
        while (running) {
            try {
                processor.process(Variables.timeout);
            } catch (Exception e) {
                Logger.error("Server has experienced an error during event processing");
                Logger.error(e);
            }
        }
    }
}
