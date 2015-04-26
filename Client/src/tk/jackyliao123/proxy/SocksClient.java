package tk.jackyliao123.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SocksClient {
    private final Tunnel tunnel;

    public SocksClient(byte[] secretKey) throws IOException {
        this.tunnel = new Tunnel(secretKey);
    }

    public static void main(String[] args) throws Exception {
        Variables.loadAllVariables(args);
    }
}
