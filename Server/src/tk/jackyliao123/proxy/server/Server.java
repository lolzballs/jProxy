package tk.jackyliao123.proxy.server;

import tk.jackyliao123.nioevent.DeathEventHandler;
import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.user.UserDataLoader;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Server {
    public final ArrayList<Connection> connections;
    public final UserDataLoader userData;
    public final EventProcessor processor;

    private final ServerSocketChannel channel;
    private final InetSocketAddress address;
    private final Selector selector;
    private final Random random;

    public Server(InetSocketAddress address) throws IOException {
        this.connections = new ArrayList<Connection>();
        this.userData = new UserDataLoader(new File("user.dat"));
        this.channel = ServerSocketChannel.open();
        this.address = address;
        this.selector = Selector.open();
        this.processor = new EventProcessor(selector);
        this.random = new SecureRandom();

        channel.bind(address);
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        while (true) {
            loop();
        }
    }

    public void loop() throws IOException {
        processor.pollEvents();

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                SocketChannel client = channel.accept();
                processor.register(client, 8, new ReadEventHandler() {
                    public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                        handshake(channel, bytes);
                    }
                });
            }

            keyIterator.remove();
        }
        selector.selectedKeys().clear();
    }

    private void handshake(SocketChannel channel, byte[] bytes) throws IOException {
        ByteBuffer write = ByteBuffer.allocate(7);
        write.put(Constants.MAGIC);
        for (int i = 0; i < Constants.MAGIC.length; i++) {
            if (bytes[i] != Constants.MAGIC[i]) {
                disconnect(channel);
                return;
            }
        }

        byte major = bytes[6];
        byte minor = bytes[7];

        if (major != Constants.MAJOR) {
            write.put(Constants.INIT_UNSUPPORTEDVERSION);

            write.flip();
            channel.write(write);
            disconnect(channel);
            return;
        }

        write.put(Constants.INIT_SUCCESSFUL);
        write.flip();
        channel.write(write);
        System.out.println("Client: " + channel.getRemoteAddress() + " connected with jProxy version: " + major + "." + minor);

        new Authenticator(this, channel, random);
    }

    public void disconnect(SocketChannel socket) {
        try {
            processor.deleteEventListener(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//        UserDataLoader users = new UserDataLoader(new File("user.dat"));
//        users.newUser("lolzballs", "lollolzballs".getBytes(Constants.CHARSET));
//        users.newUser("jackyliao123", "loljackyliao123".getBytes(Constants.CHARSET));
//        if (args.length != 0) {
//            if (args.length != 4) {
//                System.out.println("Usage: java -jar Server.jar user register <username> <password>");
//            }
//            if (args[0].equals("user")) {
//                if (args[1].equals("register")) {
//                    UserDataLoader data = new UserDataLoader(new File("user.dat"));
//                    data.newUser(args[2], args[3].getBytes());
//                    return;
//                }
//            }
//        }

        Server server = new Server(new InetSocketAddress(Constants.DEFAULT_PORT));
        server.start();
    }

}
