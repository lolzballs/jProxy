package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.server.event.ServerEventProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class Server {
    private ServerSocketChannel serverChannel;
	private ServerEventProcessor processor;

    public Server(int port) throws IOException {
        this.serverChannel = ServerSocketChannel.open();
		this.processor = new ServerEventProcessor();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
	    processor.registerServerChannel(serverChannel);
    }

    public void run() {
		processor.
    }

    public static void main(String[] args) {
        try {
            Variables.loadAllVariables(args);
            Server server = new Server(Variables.serverPort);
            server.run();
        } catch (Exception e) {
            System.err.println("Server has experienced a critical error");
            e.printStackTrace();
        }
    }
}
