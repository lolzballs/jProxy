package tk.jackyliao123.proxy.crypto;

import tk.jackyliao123.proxy.Constants;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AESCipher {
    private final Cipher encrypt;
    private final Cipher decrypt;

    public AESCipher(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.encrypt = Cipher.getInstance(Constants.AES_ALGORITHM);
        this.decrypt = Cipher.getInstance(Constants.AES_ALGORITHM);

        encrypt.init(Cipher.ENCRYPT_MODE, key);
        decrypt.init(Cipher.DECRYPT_MODE, key);
    }


    public byte[] encrypt(byte[] data) throws IOException {
        try {
            return encrypt.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public byte[] decrypt(byte[] data) throws IOException {
        try {
            return decrypt.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
