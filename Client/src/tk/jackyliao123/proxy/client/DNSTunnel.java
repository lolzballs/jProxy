package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DNSTunnel {
    private final Tunnel tunnel;
    private final HashMap<String, byte[]> cache;
    private final HashMap<String, ArrayList<DNSCallback>> callbacks;

    public DNSTunnel(Tunnel tunnel) {
        this.tunnel = tunnel;

        this.cache = new HashMap<String, byte[]>();
        this.callbacks = new HashMap<String, ArrayList<DNSCallback>>();
    }

    public void lookup(String hostname, DNSCallback callback) throws IOException {
        byte[] cached = cache.get(hostname);
        if (cached != null) {
            callback.action(Constants.DNS_SUCCESS, hostname, cached);
            return;
        }

        System.out.println(hostname);

        ArrayList<DNSCallback> ar = callbacks.get(hostname);
        if (ar == null) {
            callbacks.put(hostname, ar = new ArrayList<DNSCallback>());
        }
        ar.add(callback);

        byte[] ipstr = hostname.getBytes(Constants.CHARSET);
        int length = ipstr.length;

        byte[] data = new byte[3 + length];

        data[0] = Constants.TYPE_DNS;
        data[1] = (byte) (length >>> 8);
        data[2] = (byte) (length & 0xFF);
        System.arraycopy(ipstr, 0, data, 3, length);

        tunnel.sendEncrypted(data);
    }

    public void found(byte[] data) {
        int status = data[1];
        int length = (data[2] << 8) + (data[3] & 0xFF);

        byte[] str = new byte[length];
        System.arraycopy(data, 4, str, 0, length);
        String hostname = new String(str);

        byte[] ip = new byte[4];
        System.arraycopy(data, length + 4, ip, 0, 4);

        cache.put(hostname, ip);

        System.out.println("found: " + hostname);
        ArrayList<DNSCallback> ar = callbacks.remove(hostname);
        if (ar != null) {
            for (DNSCallback cb : ar) {
                cb.action(status, hostname, ip);
            }
        }
    }
}
