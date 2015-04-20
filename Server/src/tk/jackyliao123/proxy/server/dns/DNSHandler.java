package tk.jackyliao123.proxy.server.dns;

import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DNSHandler {
    private final Connection server;
    private final EventProcessor processor;
    private final SocketChannel client;

    private final ExecutorService executor;

    public DNSHandler(Connection server, EventProcessor processor, SocketChannel client) {
        this.server = server;
        this.processor = processor;
        this.client = client;
        executor = Executors.newFixedThreadPool(32);
    }

    public void lookup(byte[] data) throws IOException {
        executor.execute(new DNSRunnable(server, client, data));
    }
}
