package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.Constants;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.event.ReadEventListener;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthenticateResponseListener implements ReadEventListener {
    private final Tunnel tunnel;
    private final Cipher decrypt;

    public AuthenticateResponseListener(Tunnel tunnel, Cipher decrypt) {
        this.tunnel = tunnel;
        this.decrypt = decrypt;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        byte status = array[0];
        if (status != Constants.LOGIN_ACCEPTED) {
            if (status == Constants.LOGIN_INVALIDKEY) {
                throw new IOException("Incorrect key!");
            }

            throw new IOException("Server responded with an unknown status: " + status);
        }

        ByteBuffer aesEncrypted = ByteBuffer.allocate(Constants.RSA_MODULUSSIZE_BYTES);
        channel.pushFillReadBuffer(aesEncrypted, new AESKeyListener(tunnel, decrypt));
    }
}
