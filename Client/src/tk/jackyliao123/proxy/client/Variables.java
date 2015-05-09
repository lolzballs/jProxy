package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.ConfigLoader;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Variables {
    public static InetSocketAddress serverAddress;
    public static MessageDigest hashAlgorithm;
    public static long timeout;
    public static byte loggingLevel;

    public static void loadAllVariables(String[] args) throws Exception {
        // TODO: Read from args

        String ip = "localhost";
        int port = 16384;

        timeout = 60000;
        loggingLevel = Logger.INFO;

        try {
            HashMap<String, String> properties = ConfigLoader.loadConfig(new File("client.properties"));
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                if (key.equals("ip")) {
                    ip = entry.getValue();
                } else if (key.equals("port")) {
                    port = Integer.parseInt(entry.getValue());
                } else if (key.equals("timeout")) {
                    timeout = Integer.parseInt(entry.getValue());
                } else if (key.equals("loggingLevel")) {
                    String level = entry.getValue();
                    for (int i = 0; i < Logger.messages.length; i++) {
                        if (Logger.messages[i].equals(level)) {
                            loggingLevel = (byte) i;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.warning("No configuration file found, using default settings.");
        }

        serverAddress = new InetSocketAddress(ip, port);
        hashAlgorithm = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
    }
}
