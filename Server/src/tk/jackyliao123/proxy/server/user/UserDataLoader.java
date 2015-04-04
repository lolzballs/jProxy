package tk.jackyliao123.proxy.server.user;

import tk.jackyliao123.proxy.Constants;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class UserDataLoader {
    private HashMap<String, UserInfo> users;
    private File dataFile;

    public UserDataLoader(File f) throws IOException {
        dataFile = f;
        DataInputStream input = new DataInputStream(new FileInputStream(f));
        users = new HashMap<String, UserInfo>();
        int nusers = input.readInt();
        for (int i = 0; i < nusers; ++i) {
            String username = input.readUTF();
            byte[] salt = new byte[Constants.SALT_SIZE];
            input.readFully(salt);
            byte[] hash = new byte[Constants.HASH_SIZE];
            input.readFully(hash);
            UserInfo info = new UserInfo(username, salt, hash);
            users.put(username, info);
        }
        input.close();
    }

    public void save() throws IOException {
        DataOutputStream output = new DataOutputStream(new FileOutputStream(dataFile));
        int size = users.size();
        output.writeInt(size);
        for (UserInfo user : users.values()) {
            output.writeUTF(user.username);
            output.write(user.salt);
            output.write(user.hash);
        }
        output.close();
    }

    public byte[] getSalt(String username) {
        return users.get(username).salt;
    }

    public void newUser(String username, byte[] password) throws Exception {
        Random random = new SecureRandom();
        byte[] salt = new byte[Constants.SALT_SIZE];
        random.nextBytes(salt);
        byte[] salted = new byte[password.length + Constants.SALT_SIZE];
        System.arraycopy(password, 0, salted, 0, password.length);
        System.arraycopy(salt, 0, salted, password.length, Constants.SALT_SIZE);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(salted);

        for (int i = 0; i < salted.length; ++i) {
            salted[i] = 0;
        }
        for (int i = 0; i < password.length; ++i) {
            password[i] = 0;
        }

        UserInfo info = new UserInfo(username, salt, hash);

        users.put(username, info);
        save();
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public boolean authenticate(String username, byte[] finalPassword, byte[] salt) throws NoSuchAlgorithmException { // What is the density of a penis at melting point?
        UserInfo info = users.get(username);
        if (info == null) {
            return false;
        }
        MessageDigest digest = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
        byte[] salted = new byte[Constants.HASH_SIZE + Constants.SALT_SIZE];
        System.arraycopy(info.hash, 0, salted, 0, Constants.HASH_SIZE);
        System.arraycopy(salt, 0, salted, Constants.HASH_SIZE, Constants.SALT_SIZE);

        byte[] compare = digest.digest(salted);

        return Arrays.equals(compare, finalPassword);
    }
}
