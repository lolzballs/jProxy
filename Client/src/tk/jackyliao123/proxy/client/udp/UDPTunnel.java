package tk.jackyliao123.proxy.client.udp;

import com.sun.org.apache.xpath.internal.operations.Bool;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.Tunnel;

import javax.security.auth.callback.Callback;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UDPTunnel {
    private final Tunnel tunnel;
    private final UDPCallback callback;

    public UDPTunnel(Tunnel tunnel, UDPCallback callback) {
        this.tunnel = tunnel;
        this.callback = callback;
    }

    public void read(byte[] data) throws IOException {
        byte[] ip = new byte[4];
        ip[0] = data[1];
        ip[1] = data[2];
        ip[2] = data[3];
        ip[3] = data[4];
        int port = (data[5] << 8) + (data[6] & 0xFF);
        int length = (data[7] << 8) + (data[8] & 0xFF);
        byte[] payload = new byte[length];
        System.arraycopy(data, 9, payload, 0, length);

        callback.action(ip, port, payload);
    }

    public void send(byte[] ip, int port, byte[] payload) throws IOException {
        int length = payload.length;
        byte[] data = new byte[9 + length];
        data[0] = Constants.TYPE_UDP;
        data[1] = ip[0];
        data[2] = ip[1];
        data[3] = ip[2];
        data[4] = ip[3];
        data[5] = (byte) (port >>> 8);
        data[6] = (byte) (port & 0xFF);
        data[7] = (byte) (length >>> 8);
        data[8] = (byte) (length & 0xFF);
        System.arraycopy(payload, 0, data, 9, length);
        System.out.println(Arrays.toString(data));

        tunnel.sendEncrypted(data);
    }
}
