package tk.jackyliao123.proxy.server;

import tk.jackyliao123.nioevent.EventProcess;
import tk.jackyliao123.nioevent.EventProcessor;
import tk.jackyliao123.nioevent.ReadEventHandler;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.server.user.UserDataLoader;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

public class Authenticator {
    private Server server;
    private SocketChannel channel;
    private EventProcessor processor;
    private Random random;
    private int usernameLength;
    private String username;
    private UserDataLoader userData;
    private byte[] salt;

    public Authenticator(Server server, SocketChannel channel, Random random) throws IOException {
        this.server = server;
        this.channel = channel;
        this.processor = server.processor;
        this.random = random;
        this.userData = server.userData;

        processor.register(channel, 2, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                getUsernameLength(bytes);
            }
        });
    }

    public void authenticate(byte[] received) throws IOException {
        if (!userData.userExists(username)) {
            channel.write((ByteBuffer) ByteBuffer.allocate(1).put(Constants.LOGIN_INVALIDCOMBO).flip());
            server.disconnect(channel);
            return;
        }

        try {
            if (userData.authenticate(username, received, salt)) {
                channel.write((ByteBuffer) ByteBuffer.allocate(1).put(Constants.LOGIN_SUCCESSFUL).flip());

                KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RSA_ALGORITHM);
                generator.initialize(Constants.RSA_KEYSIZE);
                KeyPair pair = generator.generateKeyPair();

                // Write server public key
                channel.write(ByteBuffer.wrap(pair.getPublic().getEncoded()));

                processor.register(channel, Constants.RSA_PUBLICKEYSIZE_BYTES, new ReadEventHandler() {
                    @Override
                    public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                        try {
                            PublicKey clientPublic = KeyFactory.getInstance(Constants.RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));

                            Cipher cipher = Cipher.getInstance(Constants.RSA_ALGORITHM);
                            cipher.init(Cipher.ENCRYPT_MODE, clientPublic);

                            KeyGenerator aesGen = KeyGenerator.getInstance(Constants.AES_ALGORITHM);
                            aesGen.init(Constants.AES_KEYSIZE_BYTES);
                            SecretKey key = aesGen.generateKey();

                            // Write AES key, encrypted with cipher
                            byte[] array = cipher.doFinal(key.getEncoded());
                            channel.write(ByteBuffer.wrap(array));

                            server.connections.add(new Connection(server, channel, username, key));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                channel.write((ByteBuffer) ByteBuffer.allocate(1).put(Constants.LOGIN_INVALIDCOMBO).flip());
                server.disconnect(channel);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void getHashedPassword() throws IOException {
        processor.register(channel, Constants.HASH_SIZE, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                authenticate(bytes);
            }
        });
    }

    private void getUsernameLength(byte[] bytes) throws IOException {
        byte a = bytes[0];
        byte b = bytes[1];
        usernameLength = (a << 8) | b;

        processor.register(channel, usernameLength, new ReadEventHandler() {
            @Override
            public void action(EventProcess event, SocketChannel channel, byte[] bytes) throws IOException {
                getUsername(bytes);
                if (!userData.userExists(username)) {
                    // Fake connection to prevent people knowing username is false
                    sendSalt();
                    sendSalt();
                    getHashedPassword();
                } else {
                    sendSalt(userData.getSalt(username));
                    sendSalt();
                    getHashedPassword();
                }
            }
        });
    }

    private void sendSalt() throws IOException {
        byte[] salt = new byte[Constants.SALT_SIZE];
        random.nextBytes(salt);

        this.salt = salt;

        channel.write(ByteBuffer.wrap(salt));
    }

    private void sendSalt(byte[] salt) throws IOException {
        this.salt = salt;

        channel.write(ByteBuffer.wrap(salt));
    }

    private void getUsername(byte[] bytes) {
        username = new String(bytes);
    }
}
