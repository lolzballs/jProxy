import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Scanner;

public class KeyFileGenerator {
    public static void writeClientKeyFile(PrivateKey key, byte[] hash) throws IOException {
        File file = new File("key.dat");
        if (!file.createNewFile()) {
            throw new IOException("key.dat already exists!");
        }

        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.write(key.getEncoded());
        out.write(hash);
        out.close();
    }

    public static void writeServerKeyFile(byte[] hash, String username, byte[] salt, PublicKey key) throws IOException {
        File dir = new File("users");
        dir.mkdir();

        String pathname = "users/" + Util.bs2str(hash) + ".dat";
        File file = new File(pathname);
        if (!file.createNewFile()) {
            throw new IOException(pathname + " already exists!");
        }

        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeUTF(username);
        out.write(salt);
        out.write(key.getEncoded());
        out.close();
    }

    public static byte[] hash(PublicKey pub, byte[] salt) throws NoSuchAlgorithmException {
        byte[] key = pub.getEncoded();
        byte[] array = new byte[key.length + salt.length];
        System.arraycopy(key, 0, array, 0, key.length);
        System.arraycopy(salt, 0, array, key.length, salt.length);

        MessageDigest digest = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
        return digest.digest(array);
    }

    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return bytes;
    }

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM);
        generator.initialize(Constants.RSA_KEYSIZE);
        return generator.generateKeyPair();
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        System.out.println("jProxy v" + Constants.MAJOR + "." + Constants.MINOR + " Key File Generator");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Username: ");
        String username = scanner.nextLine();

        System.out.println("Generating random bytes...");
        byte[] salt = generateRandomBytes(Constants.SECRET_SALT_SIZE);

        System.out.println("Generating RSA key pair...");
        KeyPair pair = generateRSAKeyPair();

        System.out.println("Generating SHA256 hash...");
        byte[] hash = hash(pair.getPublic(), salt);

        System.out.println("Writing client key file...");
        writeClientKeyFile(pair.getPrivate(), hash);
        System.out.println("Writing server key file...");
        writeServerKeyFile(hash, username, salt, pair.getPublic());
        System.out.println("Done.");
    }
}
