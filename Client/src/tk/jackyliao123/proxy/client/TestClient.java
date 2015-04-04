package tk.jackyliao123.proxy.client;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try {
            final Socket socket = new Socket("localhost", 16384);
            OutputStream out = socket.getOutputStream();
            final InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            new Thread() {
                public void run() {
                    while (socket.isConnected()) {
                        byte[] bytes = new byte[1024];
                        try {
                            in.read(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (byte b : bytes) {
                            if (b == 0) {
                                continue;
                            }
                            System.out.print((char) b);
                        }
                    }
                }
            }.start();
            while (socket.isConnected()) {
                char[] line = reader.readLine().replace("\\\\", "!")
                        .toCharArray();
                for (int i = 0; i < line.length; i++) {
                    if (line[i] == '!') {
                        i++;
                        out.write((byte) (line[i] - '0'));
                        continue;
                    }
                    out.write((byte) line[i]);
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
