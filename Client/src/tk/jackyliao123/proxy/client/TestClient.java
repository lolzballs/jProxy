package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.client.event.TCPListener;
import tk.jackyliao123.proxy.event.DisconnectEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestClient {
    private final EventProcessor processor;
    private final Tunnel tunnel;
    boolean connected = true;

    public TestClient(byte[] secretKey) throws IOException {
        this.processor = new EventProcessor();
        this.tunnel = new Tunnel(processor, secretKey, new TCPListener() {
            public void onTcpConnect(int connectionId, byte statusCode, int ping) throws IOException {
                tunnel.tcp.send(0, "GET / HTTP/1.1\r\n\r\n".getBytes());
                System.out.println("connected to google with ping: " + ping + "ms");
            }

            public void onTcpPacket(int connectionId, byte[] packet) throws IOException {
                System.out.println(new String(packet));
                tunnel.tcp.send(0, "GET / HTTP/1.1\r\n\r\n".getBytes());
            }

            public void onTcpDisconnect(int connectionId, byte reason) throws IOException {
            }
        });
        tunnel.serverConnection.disconnectListener = new DisconnectEventListener() {
            public void onDisconnect(ChannelWrapper c) throws IOException {
                connected = false;
                System.out.println("Disconnected from " + c.channel);
            }
        };

        while (!tunnel.connected) {
            processor.process(10);
        }

        System.out.println("Connected successfully.");

//        tunnel.tcp.connect(0, "google.com", 80);

        while (connected) {
            processor.process(Variables.timeout);
        }
    }

    public static void main(String[] args) throws Exception {
        Logger.init(Logger.DEBUG);

        Variables.loadAllVariables(args);

        Logger.setLoggingLevel(Variables.loggingLevel);

        // Test load keys
        DataInputStream input = new DataInputStream(new FileInputStream(new File("keys.dat")));
        input.readUTF();
        byte[] key = new byte[Constants.SECRET_SALT_SIZE];
        input.readFully(key);
        new TestClient(key);
    }
}
