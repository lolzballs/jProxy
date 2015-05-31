package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.crypto.RSAKeyLoader;

import java.io.*;
import java.security.GeneralSecurityException;

public class Validator {
    public static User isValid(byte[] hash) {
        File file = new File("users/" + Util.bs2str(hash) + ".dat");

        if (!file.exists()) {
            return null;
        }

        try {
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            String username = input.readUTF();
            byte[] secretSalt = new byte[Constants.SECRET_SALT_SIZE];
            byte[] clientPub = new byte[Constants.RSA_PUBLICKEYSIZE_BYTES];
            input.readFully(secretSalt);
            input.readFully(clientPub);
            return new User(username, secretSalt, RSAKeyLoader.loadPublicKey(clientPub));
        } catch (IOException e) {
            return null;
        } catch (GeneralSecurityException e) {
            Logger.error(e);
            return null;
        }
    }
}
