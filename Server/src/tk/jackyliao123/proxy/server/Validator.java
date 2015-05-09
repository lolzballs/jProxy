package tk.jackyliao123.proxy.server;

import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;

import java.io.*;

public class Validator {
    private final File file;

    public Validator(File f) {
        this.file = f;
    }

    public String isValid(byte[] publicKey, byte[] hash) {
        byte[] secretSalt = new byte[Constants.SECRET_SALT_SIZE];
        byte[] array = new byte[Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.SECRET_SALT_SIZE];
        try {
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            while (true) {
                String user = input.readUTF();
                input.readFully(secretSalt, 0, Constants.SECRET_SALT_SIZE);
                System.arraycopy(publicKey, 0, array, 0, Constants.RSA_PUBLICKEYSIZE_BYTES);
                System.arraycopy(secretSalt, 0, array, Constants.RSA_PUBLICKEYSIZE_BYTES, Constants.SECRET_SALT_SIZE);
                byte[] newHash = Variables.hashAlgorithm.digest(array);
                if (newHash.length != hash.length || hash.length != Constants.HASH_SIZE) {
                    Logger.error("Hash length mismatch: received " + hash.length + " bytes, generated " + newHash.length + " bytes, expected " + Constants.HASH_SIZE + " bytes");
                    continue;
                }
                if (Util.bseq(hash, newHash)) {
                    return user;
                } else {
                    Logger.error("Invalid hash received: " + Util.bs2str(hash));
                }
            }
        } catch (EOFException ignored) {
        } catch (IOException e) {
            Logger.error("Error occurred when reading secret keys: " + e);
        } finally {
            for (int i = 0; i < secretSalt.length; ++i) {
                secretSalt[i] = 0;
            }
            for (int i = 0; i < array.length; ++i) {
                array[i] = 0;
            }
        }
        Logger.error("No match for received hash found: " + Util.bs2str(hash));
        return null;
    }
}
