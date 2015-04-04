package tk.jackyliao123.proxy.client;

import tk.jackyliao123.proxy.Constants;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class Connector {
    public static SecretKey connect(SocketChannel channel, String username, byte[] password) throws Exception {
        channel.configureBlocking(true);

        init(channel);
        login(channel, username, password);
        return exchangeKeys(channel);
    }

    private static void init(SocketChannel channel) throws Exception {
        channel.write((ByteBuffer) ByteBuffer.allocate(8).put(Constants.MAGIC).put(Constants.MAJOR).put(Constants.MINOR).flip());

        ByteBuffer magic = ByteBuffer.allocate(7);
        channel.read(magic);
        byte[] magicBytes = magic.array();
        for (int i = 0; i < 6; i++) {
            if (magicBytes[i] != Constants.MAGIC[i]) {
                return;
            }
        }
        byte code = magicBytes[6];
        if (code == Constants.INIT_SUCCESSFUL) {
            System.out.println("Initalized connection.");
        } else if (code == Constants.INIT_UNSUPPORTEDVERSION) {
            throw new Exception("Unsupported Version");
        }
    }

    private static void login(SocketChannel channel, String username, byte[] password) throws Exception {
        sendUsername(channel, username);
        sendPassword(channel, password);

        ByteBuffer response = ByteBuffer.allocate(1);
        channel.read(response);
        response.flip();
        byte res = response.get();
        if (res == Constants.LOGIN_INVALIDCOMBO) {
            throw new Exception("Invalid username or password.");
        }

        System.out.println("Logged in successfully!");
    }

    private static void sendUsername(SocketChannel channel, String username) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2 + username.length());
        byte[] b = username.getBytes(Constants.CHARSET);
        buffer.putShort((short) b.length);
        buffer.put(b);
        buffer.flip();
        channel.write(buffer);
    }


    private static void sendPassword(SocketChannel channel, byte[] password) throws IOException {
        ByteBuffer storedSalt = ByteBuffer.allocate(Constants.SALT_SIZE);
        channel.read(storedSalt);
        byte[] stored = storedSalt.array();
        ByteBuffer randomSalt = ByteBuffer.allocate(Constants.SALT_SIZE);
        channel.read(randomSalt);
        byte[] random = randomSalt.array();
        byte[] salt1 = new byte[password.length + Constants.SALT_SIZE];
        System.arraycopy(password, 0, salt1, 0, password.length);
        System.arraycopy(stored, 0, salt1, password.length, Constants.SALT_SIZE);

        try {
            MessageDigest digest = MessageDigest.getInstance(Constants.HASH_ALGORITHM);
            byte[] hashed = digest.digest(salt1);
            byte[] salt2 = new byte[Constants.HASH_SIZE + Constants.SALT_SIZE];
            System.arraycopy(hashed, 0, salt2, 0, Constants.HASH_SIZE);
            System.arraycopy(random, 0, salt2, Constants.HASH_SIZE, Constants.SALT_SIZE);

            byte[] finaled = digest.digest(salt2);
            channel.write(ByteBuffer.wrap(finaled));
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    private static SecretKey exchangeKeys(SocketChannel channel) throws Exception {
        ByteBuffer serverPublic = ByteBuffer.allocate(Constants.RSA_PUBLICKEYSIZE_BYTES);
        channel.read(serverPublic);
        byte[] array = serverPublic.array();
        PublicKey serverKey = KeyFactory.getInstance(Constants.RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(array));

        KeyPair keys = generateKeys();
        PrivateKey clientPrivate = keys.getPrivate();
        PublicKey clientPublic = keys.getPublic();

        channel.write(ByteBuffer.wrap(clientPublic.getEncoded()));

        ByteBuffer aesEncrypted = ByteBuffer.allocate(Constants.RSA_MODULUSSIZE_BYTES);
        channel.read(aesEncrypted);
        Cipher rsaDecrypt = Cipher.getInstance(Constants.RSA_ALGORITHM);
        rsaDecrypt.init(Cipher.DECRYPT_MODE, clientPrivate);
        byte[] aesBytes = rsaDecrypt.doFinal(aesEncrypted.array());

        return new SecretKeySpec(aesBytes, Constants.AES_ALGORITHM);
    }

    private static KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM);
        generator.initialize(Constants.RSA_KEYSIZE);
        return generator.generateKeyPair();
    }
}
