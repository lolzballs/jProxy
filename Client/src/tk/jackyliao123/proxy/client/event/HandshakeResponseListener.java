package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.Util;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.client.Variables;
import tk.jackyliao123.proxy.crypto.RSAKeyLoader;
import tk.jackyliao123.proxy.event.ReadEventListener;

import javax.crypto.Cipher;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class HandshakeResponseListener implements ReadEventListener {
    private Tunnel tunnel;

    public HandshakeResponseListener(Tunnel tunnel) {
        this.tunnel = tunnel;
    }

    @Override
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
            Logger.info("Connected to server. Authenticating.");
        }

        try {
            DataInputStream input = new DataInputStream(new FileInputStream(Variables.secretFile));
            // Get keys
            Logger.verbose("Retrieving RSA private key... ");
            byte[] encoded = new byte[input.readInt()];
            input.readFully(encoded);
            Cipher decrypt = Cipher.getInstance(Constants.RSA_ALGORITHM);
            decrypt.init(Cipher.DECRYPT_MODE, RSAKeyLoader.loadPrivateKey(encoded));

            Logger.verbose("Retrieving key signature hash... ");

            byte[] hash = new byte[Constants.HASH_SIZE];
            input.readFully(hash, 0, Constants.HASH_SIZE);
            Logger.verbose("complete");

            ByteBuffer auth = ByteBuffer.allocate(Constants.HASH_SIZE);
            auth.put(hash);
            auth.flip();
            channel.pushWriteBuffer(auth);

            Logger.verbose("Sending...");

            // Server response
            ByteBuffer statusResponse = ByteBuffer.allocate(1);
            channel.pushFillReadBuffer(statusResponse, new AuthenticateResponseListener(tunnel, decrypt));
        } catch (Exception e) {
            Logger.error("Error occurred when exchanging keys");
            throw new IOException(e);
        }
    }
}

