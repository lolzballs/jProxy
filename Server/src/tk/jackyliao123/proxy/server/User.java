package tk.jackyliao123.proxy.server;

import java.security.PrivateKey;
import java.security.PublicKey;

public class User {
    public final String username;
    public final byte[] secretSalt;
    public final PublicKey clientPub;

    public User(String username, byte[] secretSalt, PublicKey clientPub) {
        this.username = username;
        this.secretSalt = secretSalt;
        this.clientPub = clientPub;
    }
}
