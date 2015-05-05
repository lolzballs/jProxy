package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.client.Variables;
import tk.jackyliao123.proxy.event.ReadEventListener;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class HandshakeResponseListener implements ReadEventListener {
    private Tunnel tunnel;
    private byte[] secretKey;

    public HandshakeResponseListener(Tunnel tunnel, byte[] secretKey) {
        this.tunnel = tunnel;
        this.secretKey = secretKey;
    }

    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        boolean eq = Util.bseq(array, 0, Constants.MAGIC_LENGTH, Constants.MAGIC, 0, Constants.MAGIC_LENGTH);
        if (!eq) {
            throw new IOException("Error: Server Client Magic Mismatch");
        }
        byte status = array[Constants.MAGIC_LENGTH];
        if (status != Constants.INIT_SUCCESSFUL) {
            if (status == Constants.INIT_UNSUPPORTEDVERSION) {
                throw new IOException("Error: Unsupported version.");
            }
            throw new IOException("Error: Invalid Response: " + status);
        } else {
            System.out.println("Connected successfully.");
        }

        try {
            // Generate keys
            KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM);
            generator.initialize(Constants.RSA_KEYSIZE);
            KeyPair rsaPair = generator.generateKeyPair();
            Cipher decrypt = Cipher.getInstance(Constants.RSA_ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, rsaPair.getPrivate());
            byte[] encoded = rsaPair.getPublic().getEncoded();

            // Generate hash
            byte[] toBeHashed = new byte[Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.SECRET_SALT_SIZE];
            System.arraycopy(encoded, 0, toBeHashed, 0, Constants.RSA_PUBLICKEYSIZE_BYTES);
            System.arraycopy(secretKey, 0, toBeHashed, Constants.RSA_PUBLICKEYSIZE_BYTES, Constants.SECRET_SALT_SIZE);

            byte[] hash = Variables.hashAlgorithm.digest(toBeHashed);

            ByteBuffer auth = ByteBuffer.allocate(Constants.RSA_PUBLICKEYSIZE_BYTES + Constants.HASH_SIZE);
            auth.put(encoded);
            auth.put(hash);
            auth.flip();
            channel.pushWriteBuffer(auth);

            // Server response
            ByteBuffer statusResponse = ByteBuffer.allocate(1);
            channel.pushFillReadBuffer(statusResponse, new AuthenticateResponseListener(tunnel, decrypt));
        } catch (Exception e) {
            System.err.println("Error occurred when exchanging keys");
            throw new IOException(e);
        }


    }
}

