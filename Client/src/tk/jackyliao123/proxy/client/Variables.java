package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;

import java.net.InetSocketAddress;
import java.security.MessageDigest;

public class Variables {
    public static InetSocketAddress serverAddress;
    public static MessageDigest hashAlgorithm;
    public static long timeout;
//    public static

    public static void loadAllVariables(String[] args) throws Exception {
        // TODO: Read from args + config files
        serverAddress = new InetSocketAddress("192.168.88.238", 16384);
        timeout = 60000;
        hashAlgorithm = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
    }
}
