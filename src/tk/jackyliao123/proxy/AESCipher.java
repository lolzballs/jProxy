package tk.jackyliao123.proxy;

import javax.crypto.*;
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


    public byte[] encrypt(byte[] data) {
        try {
            return encrypt.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] decrypt(byte[] data) {
        try {
            return decrypt.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
