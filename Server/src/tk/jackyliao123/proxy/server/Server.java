package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.event.AcceptEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.event.AuthenticateListener;
import tk.jackyliao123.proxy.server.event.HandshakeListener;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.security.SecureRandom;
import java.util.Random;

public class Server implements AcceptEventListener {
    private ServerSocketChannel serverChannel;
    private EventProcessor processor;
    private Validator validator;
    private Random random;
    private boolean running;

    public Server(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
        this.processor = new EventProcessor();
        this.validator = new Validator(Variables.secretKeyFile);
        this.random = new SecureRandom();

        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        processor.registerServerChannel(serverChannel, this);
    }

    @Override
    public void onAccept(ChannelWrapper channel) throws IOException {
        processor.fillArrayToMax(channel, Constants.MAGIC_LENGTH + 2, new HandshakeListener());
        processor.fillArrayToMax(channel, Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.HASH_SIZE, new AuthenticateListener(validator, random));

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
