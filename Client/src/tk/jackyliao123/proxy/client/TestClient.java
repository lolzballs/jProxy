package tk.jackyliao123.proxy.client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;

public class TestClient {
    public static Tunnel tunnel;
    public static WriteThread[] wThreads = new WriteThread[65536];
    public static ReadThread[] rThreads = new ReadThread[65536];

    public static void main(String[] args) {
        try {
            tunnel = new Tunnel(new InetSocketAddress("localhost", 16384), "lolzballs", "lollolzballs".getBytes("UTF-8"), new ReadCallback() {
                @Override
                public void action(TCPConnection connection, byte[] data) {
                    if (data.length == 0) {
                        rThreads[connection.id].connectionStatus = connection.status;
                    } else {
                        wThreads[connection.id].dataQueue.addLast(data);
                    }
                }
            });
            new Thread() {
                @Override
                public void run() {
                    try {
                        tunnel.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            ServerSocket sSocket = new ServerSocket(9715);
            while (true) {
                Socket s = sSocket.accept();
                ReadThread t = new ReadThread(s);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int id = 0;

    public static class ReadThread extends Thread {
        public Socket s;
        public InputStream rawInput;
        public OutputStream rawOutput;
        public BufferedReader reader;
        public int cid;

        public int connectionStatus;

        public ReadThread(Socket s) throws IOException {
            this.s = s;
            rawInput = s.getInputStream();
            rawOutput = s.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(rawInput));
        }

        public void run() {
            try {
                String s = reader.readLine();
                String request = s;
                while (reader.readLine().length() != 0) {
                }
                request = request.replace("CONNECT ", "").replace(" HTTP/1.1", "");
                String[] split = request.split(":");
                String host = split[0];
                int port = Integer.parseInt(split[1]);
                System.out.println(host + ", " + port);

                cid = id++;

                id %= 65536;

                rThreads[cid] = this;

                tunnel.tcp.createConnection(InetAddress.getByName(host).getAddress(), port, cid);

                while (connectionStatus == 0) {
                    Thread.sleep(50);
                }

                if (connectionStatus != 1) {
                    rawOutput.write("HTTP/1.1 502 Bad Fucking Gateway\r\n\r\n".getBytes());
                    return;
                }

                rawOutput.write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes());

                wThreads[cid] = new WriteThread(this.s);
                wThreads[cid].start();

                byte[] buffer = new byte[2048];

                int read;

                while ((read = rawInput.read(buffer, 0, 2048)) != -1) {
                    byte[] b = new byte[read];
                    System.arraycopy(buffer, 0, b, 0, read);
                    tunnel.tcp.send(b, cid);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class WriteThread extends Thread {
        public Socket s;
        public OutputStream output;
        public ArrayDeque<byte[]> dataQueue = new ArrayDeque<byte[]>();

        public WriteThread(Socket s) throws IOException {
            this.s = s;
            output = s.getOutputStream();
        }

        public void run() {
            try {
                while (true) {
                    while (!dataQueue.isEmpty()) {
                        byte[] arrayToSend = dataQueue.removeFirst();
                        output.write(arrayToSend);
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
            }
        }

    }

}
