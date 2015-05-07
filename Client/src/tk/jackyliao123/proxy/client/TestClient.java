package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.event.TCPListener;
import tk.jackyliao123.proxy.event.DisconnectEventListener;
import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestClient {
    private final Tunnel tunnel;
    private final EventProcessor processor;
    boolean connected = true;

    public TestClient(byte[] secretKey) throws IOException {
        this.processor = new EventProcessor();
        this.tunnel = new Tunnel(processor, secretKey, new TCPListener() {
            public void onTcpConnect(int connectionId, byte statusCode, int ping) {

            }

            public void onTcpPacket(int connectionId, byte[] packet) {
                System.out.println(new String(packet));
            }

            public void onTcpDisconnect(int connectionId, byte reason) {

            }
        });
        tunnel.serverConnection.disconnectListener = new DisconnectEventListener() {
            public void onDisconnect(ChannelWrapper c) throws IOException {
                connected = false;
            }
        };
        while (connected) {
            processor.process(Variables.timeout);
        }
    }

    public static void main(String[] args) throws Exception {
        Variables.loadAllVariables(args);

        // Test load keys
        DataInputStream input = new DataInputStream(new FileInputStream(new File("keys.dat")));
        input.readUTF();
        byte[] key = new byte[Constants.SECRET_SALT_SIZE];
        input.readFully(key);
        new TestClient(key);
    }
}
