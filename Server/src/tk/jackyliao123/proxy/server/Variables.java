package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.Constants;

import java.io.File;
import java.security.MessageDigest;

public class Variables {
    public static int serverPort;
    public static File secretKeyFile;
    public static MessageDigest hashAlgorithm;

    public static void loadAllVariables(String[] args) throws Exception {
        serverPort = 443;
        secretKeyFile = new File("keys.dat");
        hashAlgorithm = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
    }
}
