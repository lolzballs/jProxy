package tk.jackyliao123.proxy.client;

import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.proxy.Constants;

import java.io.IOException;
import java.util.HashMap;

public class TCPTunnel {
    private final Tunnel tunnel;
    private final HashMap<Integer, Integer> ports; // key=port, value=id
    private final Connection[] connections;

    public TCPTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;
        this.ports = new HashMap<Integer, Integer>();

        this.connections = new Connection[Constants.MAX_CONNECTIONS];
    }

    public void connect(byte[] data) {
        byte status = data[1];
        int id = (data[2] << 8) | data[3];

        connections[id].status = status;
        switch (status) {
            case Constants.TCP_CONNECTION_OK:
                System.out.println(id + " OK");
                break;
            case Constants.TCP_CONNECTION_TIMEOUT:
                System.out.println(id + " TIMEOUT");
                break;
            case Constants.TCP_CONNECTION_REFUSED:
                System.out.println(id + " REFUSED");
                break;
            case Constants.TCP_CONNECTION_UNREACHABLE:
                System.out.println(id + " UNREACHABLE");
                break;
            case Constants.TCP_CONNECTION_OTHER:
                System.out.println(id + " OTHER");
                break;
        }
    }

    public void disconnect(byte[] data) {
        int id = (data[1] << 8) | data[2];
        System.out.println(id + " DISCONNECT");
    }

    public void read(byte[] data) {

    }

    public void createConnection(byte[] ip, int port, int id) throws IOException {
        byte[] data = new byte[9];
        data[0] = 0x41;
        data[1] = ip[0];
        data[2] = ip[1];
        data[3] = ip[2];
        data[4] = ip[3];
        data[5] = (byte) (port >> 8);
        data[6] = (byte) (port & 0xFF);
        data[7] = (byte) (id >> 8);
        data[8] = (byte) (id & 0xFF);

        tunnel.sendEncrypted(data);
        ports.put(port, id);
        connections[id] = new Connection(id, ip, port);
    }

    public void send(byte[] data, int id) throws IOException {
        byte[] payload = new byte[data.length + 5];
        payload[0] = 1;
        payload[1] = (byte) (id >> 8);
        payload[2] = (byte) (id & 0xFF);
        payload[3] = (byte) (data.length >> 8);
        payload[4] = (byte) (data.length & 0xFF);

        System.arraycopy(data, 0, payload, 5, data.length);

        tunnel.sendEncrypted(payload);
    }
}
