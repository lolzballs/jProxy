package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;

import java.io.IOException;

public class TCPTunnel {
    private final Tunnel tunnel;
    private final TCPConnection[] connections;
    private final ReadCallback callback;

    public TCPTunnel(Tunnel tunnel, ReadCallback callback) {
        this.tunnel = tunnel;
        this.callback = callback;

        this.connections = new TCPConnection[Constants.MAX_CONNECTIONS];
    }

    public void connect(byte[] data) {
        byte status = data[1];
        int id = (data[2] << 8) + (data[3] & 0xFF);

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

        callback.action(connections[id], new byte[]{});
    }

    public void disconnect(byte[] data) {
        int id = (data[1] << 8) + (data[2] & 0xFF);
        System.out.println(id + " DISCONNECT");
    }

    public void read(byte[] data) {
        int id = (data[1] << 8) + (data[2] & 0xFF);
        int length = (data[3] << 8) + (data[4] & 0xFF);

        byte[] payload = new byte[length];
        System.arraycopy(data, 5, payload, 0, length);

        callback.action(connections[id], payload);
    }

    public void createConnection(byte[] ip, int port, int id) throws IOException {
        byte[] data = new byte[9];
        data[0] = 0x41;
        data[1] = ip[0];
        data[2] = ip[1];
        data[3] = ip[2];
        data[4] = ip[3];
        data[5] = (byte) (port >>> 8);
        data[6] = (byte) (port & 0xFF);
        data[7] = (byte) (id >>> 8);
        data[8] = (byte) (id & 0xFF);

        tunnel.sendEncrypted(data);
        connections[id] = new TCPConnection(id, ip, port);
    }

    public void send(byte[] data, int id) throws IOException {
        byte[] payload = new byte[data.length + 5];
        payload[0] = 1;
        payload[1] = (byte) (id >>> 8);
        payload[2] = (byte) (id & 0xFF);
        payload[3] = (byte) (data.length >>> 8);
        payload[4] = (byte) (data.length & 0xFF);

        System.arraycopy(data, 0, payload, 5, data.length);

        tunnel.sendEncrypted(payload);
    }
}
