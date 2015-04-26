package tk.jackyliao123.proxy;

import tk.jackyliao123.proxy.cipher.AESCipher;
import tk.jackyliao123.proxy.event.EventProcessor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.util.Arrays;

public class Tunnel {
    private final SocketChannel serverConnection;
    private final EventProcessor processor;
    private final AESCipher cipher;

    public Tunnel(byte[] secretKey) throws IOException {
        this.serverConnection = SocketChannel.open(Variables.serverAddress);
        this.processor = new EventProcessor();
        this.cipher = init(secretKey);

        serverConnection.configureBlocking(false);
    }

    private AESCipher init(byte[] secretKey) throws IOException {
        ByteBuffer handshake = ByteBuffer.allocate(Constants.MAGIC_LENGTH + 2);
        handshake.put(Constants.MAGIC);
        handshake.put(Constants.MAJOR);
        handshake.put(Constants.MINOR);
        serverConnection.write((ByteBuffer) handshake.flip());

        ByteBuffer handshakeResponse = ByteBuffer.allocate(Constants.MAGIC_LENGTH + 1);
        serverConnection.read(handshakeResponse);
        byte[] handshakeData = handshakeResponse.array();

        if (!Util.bseq(handshakeData, 0, Constants.MAGIC_LENGTH, Constants.MAGIC, 0, Constants.MAGIC_LENGTH)) {
            System.err.println("Expected: " + Arrays.toString(Constants.MAGIC) + ", but received: " + Arrays.toString(Arrays.copyOf(handshakeData, Constants.MAGIC_LENGTH)));
            throw new IOException("Server magic mismatched!");
        }

        int status = handshakeData[Constants.MAGIC_LENGTH];
        if (status != Constants.INIT_SUCCESSFUL) {
            if (status == Constants.INIT_UNSUPPORTEDVERSION) {
                System.err.println("Server reported unsupported version.");
                throw new IOException("Server doesn't support this version.");
            }

            System.err.println("Unknown response from handshake: " + status);
            throw new IOException("Server responded with an unknown status: " + status);
        }

        try {
            KeyPair rsaPair = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM).generateKeyPair();
            Cipher decrypt = Cipher.getInstance(Constants.RSA_ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, rsaPair.getPrivate());
            byte[] encoded = rsaPair.getPublic().getEncoded();

            byte[] toBeHashed = new byte[Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.SECRET_SALT_SIZE];
            System.arraycopy(encoded, 0, toBeHashed, 0, Constants.RSA_PUBLICKEYSIZE_BYTES);
            System.arraycopy(secretKey, 0, toBeHashed, Constants.RSA_PUBLICKEYSIZE_BYTES, Constants.SECRET_SALT_SIZE);

            byte[] hash = Variables.hashAlgorithm.digest(toBeHashed);

            ByteBuffer auth = ByteBuffer.allocate(Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.HASH_SIZE);
            auth.put(encoded);
            auth.put(hash);
            serverConnection.write((ByteBuffer) auth.flip());

            ByteBuffer statusResponse = ByteBuffer.allocate(1);
            serverConnection.read(statusResponse);
            statusResponse.flip();

            byte authStatus = statusResponse.get();
            if (authStatus != Constants.LOGIN_ACCEPTED) {
                if (authStatus == Constants.LOGIN_INVALIDKEY) {
                    throw new IOException("Incorrect Key!");
                }

                System.err.println("Unknown response from authentication: " + status);
                throw new IOException("Server responded with an unknown status: " + status);
            }

            ByteBuffer aesEncrypted = ByteBuffer.allocate(Constants.RSA_MODULUSSIZE_BYTES);
            serverConnection.read(aesEncrypted);
            aesEncrypted.flip();

            byte[] aesBytes = decrypt.doFinal(aesEncrypted.array());
            return new AESCipher(new SecretKeySpec(aesBytes, Constants.AES_ALGORITHM));
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
