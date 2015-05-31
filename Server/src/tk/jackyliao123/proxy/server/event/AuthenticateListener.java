package tk.jackyliao123.proxy.server.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.Logger;
import tk.jackyliao123.proxy.crypto.AESCipher;
import tk.jackyliao123.proxy.event.ReadEventListener;
import tk.jackyliao123.proxy.server.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class AuthenticateListener implements ReadEventListener {
    public final Server server;

    public AuthenticateListener(Server server) {
        this.server = server;
    }

    public void sendAccepted(ChannelWrapper channel, PublicKey clientKey, byte[] aesKey) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, clientKey);
            byte[] data = cipher.doFinal(aesKey);

            ByteBuffer b = ByteBuffer.allocate(1 + Constants.RSA_MODULUSSIZE_BYTES);
            b.put(Constants.LOGIN_ACCEPTED);
            b.put(data);
            channel.pushWriteBuffer((ByteBuffer) b.flip());
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public void sendInvalid(ChannelWrapper channel) {
        ByteBuffer b = ByteBuffer.allocate(1);
        b.put(Constants.LOGIN_INVALIDKEY);
        channel.pushWriteBuffer((ByteBuffer) b.flip());
        channel.closeOnFinishData();
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte[] hash = new byte[Constants.HASH_SIZE];
        System.arraycopy(array, Constants.RSA_PUBLICKEYSIZE_BYTES, hash, 0, Constants.HASH_SIZE);

        User user = Validator.isValid(hash);
        if (user == null) {
            sendInvalid(channel);
            return;
        }

        try {
            Logger.verbose("Generating AES Key...");
            KeyGenerator generator = KeyGenerator.getInstance(Constants.AES_ALGORITHM);
            generator.init(Constants.AES_KEYSIZE);
            SecretKey key = generator.generateKey();
            byte[] aesKey = key.getEncoded();

            ServerTunnelChannelWrapper tunnelWrapper = new ServerTunnelChannelWrapper(channel);
            ClientConnection connection = new ClientConnection(server, tunnelWrapper, new AESCipher(key));
            tunnelWrapper.connections = connection.tcp.tcpConnections;
            server.connections.put(user.username, connection);
            sendAccepted(tunnelWrapper, user.clientPub, aesKey);
            tunnelWrapper.pushFillReadBuffer(ByteBuffer.allocate(1), connection.packetLengthListener);

            Logger.info("User " + user.username + " connected from " + channel.channel);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
