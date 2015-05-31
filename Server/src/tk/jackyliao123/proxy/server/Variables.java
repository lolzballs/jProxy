package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.ConfigLoader;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Variables {
    public static File privateKeyFile;
    public static File publicKeyFile;
    public static int serverPort;
    public static File secretKeyFile;
    public static MessageDigest hashAlgorithm;
    public static long timeout;
    public static byte loggingLevel;

    public static void loadAllVariables(String[] args) throws Exception {
        // TODO: Read from args + config files

        String privateKeyPath = "private.dat";
        String publicKeyPath = "public.dat";
        String keyFilePath = "keys.dat";

        serverPort = 16384;
        timeout = 60000;
        loggingLevel = Logger.INFO;

        try {
            HashMap<String, String> properties = ConfigLoader.loadConfig(new File("server.properties"));
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                if (key.equals("port")) {
                    serverPort = Integer.parseInt(entry.getValue());
                } else if (key.equals("timeout")) {
                    timeout = Integer.parseInt(entry.getValue());
                } else if (key.equals("keyFile")) {
                    keyFilePath = entry.getValue();
                } else if (key.equals("loggingLevel")) {
                    String level = entry.getValue();
                    for (int i = 0; i < Logger.messages.length; i++) {
                        if (Logger.messages[i].equals(level)) {
                            loggingLevel = (byte) i;
                            break;
                        }
                    }
                } else if (key.equals("privateKeyPath")) {
                    privateKeyPath = entry.getValue();
                } else if (key.equals("publicKeyPath")) {
                    publicKeyPath = entry.getValue();
                }
            }
        } catch (IOException e) {
            Logger.warning("No configuration file found, using default settings.");
        }

        privateKeyFile = new File(privateKeyPath);
        publicKeyFile = new File(publicKeyPath);
        secretKeyFile = new File(keyFilePath);
        hashAlgorithm = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
    }
}
