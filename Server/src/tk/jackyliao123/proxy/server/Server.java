package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.event.*;
import tk.jackyliao123.proxy.server.event.AuthenticateListener;
import tk.jackyliao123.proxy.server.event.EncryptedPacketLengthListener;
import tk.jackyliao123.proxy.server.event.EncryptedPacketListener;
import tk.jackyliao123.proxy.server.event.HandshakeListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;

public class Server implements AcceptEventListener {
    private ServerSocketChannel serverChannel;
    private EventProcessor processor;
    private boolean running;

    public Validator validator;
    public HashMap<String, ClientConnection> connections;

    public HandshakeListener handshakeListener;
    public AuthenticateListener authenticateListener;

    public Server(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.processor = new EventProcessor();

        this.validator = new Validator(Variables.secretKeyFile);
        this.connections = new HashMap<String, ClientConnection>();

        this.handshakeListener = new HandshakeListener(this);
        this.authenticateListener = new AuthenticateListener(this);

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        processor.registerServerChannel(serverChannel, this);
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
                System.err.println("Server has experienced an error during event processing");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Variables.loadAllVariables(args);
            Server server = new Server(Variables.serverPort);
            server.start();
        } catch (Exception e) {
            System.err.println("Server has experienced a critical error");
            e.printStackTrace();
        }
    }
}
