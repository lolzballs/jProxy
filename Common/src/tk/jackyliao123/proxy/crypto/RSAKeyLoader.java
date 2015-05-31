package tk.jackyliao123.proxy.crypto;

import tk.jackyliao123.proxy.Constants;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSAKeyLoader {
    public static PublicKey loadPublicKey(byte[] bytes) throws GeneralSecurityException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(Constants.RSA_ALGORITHM);
        return kf.generatePublic(spec);
    }

    public static PrivateKey loadPrivateKey(byte[] bytes) throws GeneralSecurityException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance(Constants.RSA_ALGORITHM);
        return kf.generatePrivate(spec);
    }
}
