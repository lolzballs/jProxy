package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.event.EventProcessor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestClient {
    private final Tunnel tunnel;
    private final EventProcessor processor;

    public TestClient(byte[] secretKey) throws IOException {
        this.processor = new EventProcessor();
        this.tunnel = new Tunnel(processor, secretKey);
    }

    public static void main(String[] args) throws Exception {
        Variables.loadAllVariables(args);

        // Test code
        DataInputStream input = new DataInputStream(new FileInputStream(new File("keys.dat")));
        input.readUTF();
        byte[] key = new byte[Constants.SECRET_SALT_SIZE];
        input.readFully(key);
        new TestClient(key);
    }
}
