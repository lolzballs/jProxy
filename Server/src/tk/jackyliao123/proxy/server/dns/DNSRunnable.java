package tk.jackyliao123.proxy.server.dns;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;

public class DNSRunnable implements Runnable {
    public ArrayDeque<String> queries;
    private final Connection server;
    private final SocketChannel client;
    private final byte[] data;

    public DNSRunnable(Connection server, SocketChannel client, byte[] data) {
        queries = new ArrayDeque<String>();
        this.server = server;
        this.client = client;
        this.data = data;
    }

    public void run() {
        try {
            int length = (data[1] << 8) + (data[2] & 0xFF);
            byte[] dm = new byte[length];
            System.arraycopy(data, 3, dm, 0, length);
            String domain = new String(dm);

            byte[] ip = new byte[]{0, 0, 0, 0};
            byte[] send = new byte[8 + length];

            byte status;
            try {
                ip = InetAddress.getByName(domain).getAddress();

                status = Constants.DNS_SUCCESS;
            } catch (UnknownHostException e) {
                status = Constants.DNS_FAIL;
            }

            send[0] = Constants.TYPE_DNS;
            send[1] = status;
            send[2] = data[1];
            send[3] = data[2];
            System.arraycopy(dm, 0, send, 4, length);
            System.arraycopy(ip, 0, send, length + 4, 4);

            server.sendEncrypted(client, send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
