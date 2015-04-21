package tk.jackyliao123.proxy.server.udp;

import tk.jackyliao123.nioevent.DeathEventHandler;
import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.Connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class UDPHandler {
    private final Connection server;
    private final EventProcessor processor;
    private final SocketChannel client;
    private final DeathEventHandler deathHandler;
    private final HashMap<String, DatagramChannel> channels;

    public UDPHandler(Connection server, EventProcessor processor, SocketChannel client) {
        this.server = server;
        this.processor = processor;
        this.client = client;

        this.deathHandler = new DeathEventHandler() {
            @Override
            public void action(EventProcess event) throws IOException {
                DatagramChannel channel = (DatagramChannel) event.key.channel();
                String host = channel.getRemoteAddress().toString();
                System.out.println(host);
                channels.remove(host);
            }
        };
        this.channels = new HashMap<String, DatagramChannel>();
    }

    public void send(byte[] data) {
        byte[] ip = new byte[4];
        System.arraycopy(data, 1, ip, 0, 4);
        int port = (data[5] << 8) + (data[6] & 0xFF);
        int length = (data[7] << 8) + (data[8] & 0xFF);
        byte[] writeData = new byte[length];
        System.arraycopy(data, 9, writeData, 0, length);

        try {
            InetSocketAddress address = new InetSocketAddress(InetAddress.getByAddress(ip), port);
            DatagramChannel channel = channels.get(address.toString());
            if (channel == null) {
                channel = DatagramChannel.open();
                channel.connect(address);
                channels.put(address.toString(), channel);
                read(address);
            }

            channel.write(ByteBuffer.wrap(writeData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(final InetSocketAddress address) throws IOException {
        DatagramChannel channel = channels.get(address.toString());
        if (channel == null) {
            return;
        }

        processor.register(address.toString(), channel, new ReadEventHandler() {
            @Override
            public void action(EventProcess process, ByteChannel channel, byte[] bytes) throws IOException {
                byte[] ip = address.getAddress().getAddress();
                int port = address.getPort();
                int length = bytes.length;
                byte[] data = new byte[length + 9];
                data[0] = Constants.TYPE_UDP;
                data[1] = ip[0];
                data[2] = ip[1];
                data[3] = ip[2];
                data[4] = ip[3];
                data[5] = (byte) (port >>> 8);
                data[6] = (byte) (port & 0xFF);
                data[7] = (byte) (length >>> 8);
                data[8] = (byte) (length & 0xFF);

                System.arraycopy(bytes, 0, data, 9, length);
                server.sendEncrypted(client, data);

                read(address);
            }
        }, deathHandler);
    }
}
