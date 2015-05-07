package tk.jackyliao123.proxy.client.event;

import tk.jackyliao123.proxy.ChannelWrapper;
import tk.jackyliao123.proxy.client.Tunnel;
import tk.jackyliao123.proxy.event.ReadEventListener;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AESKeyListener implements ReadEventListener {
    private final Tunnel tunnel;
    private final Cipher decrypt;

    public AESKeyListener(Tunnel tunnel, Cipher decrypt) {
        this.tunnel = tunnel;
        this.decrypt = decrypt;
    }

    @Override
    public void onRead(ChannelWrapper channel, byte[] array) throws IOException {
        try {
            byte[] aesBytes = decrypt.doFinal(array);
            tunnel.init(aesBytes);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
