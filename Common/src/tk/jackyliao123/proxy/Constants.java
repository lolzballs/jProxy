package tk.jackyliao123.proxy;

public class Constants {
    // Handshake Constants
    public static final byte[] MAGIC = new byte[]{'j', 'P', 'r', 'o', 'x', 'y'};
    public static final byte MAJOR = 3;
    public static final byte MINOR = 0;
    public static final int MAGIC_LENGTH = MAGIC.length;

    // Initalization return codes
    public static final byte INIT_SUCCESSFUL = 0;
    public static final byte INIT_UNSUPPORTEDVERSION = 1;

    // Login return codes
    public static final byte LOGIN_ACCEPTED = 0;
    public static final byte LOGIN_INVALIDKEY = 1;

    // Connection Address Types
    public static final byte IPv4 = 1;
    public static final byte IPv6 = 2;
    public static final byte DNS = 3;

    //Connections
    public static final byte TCP_CONNECT = 0x01;
    public static final byte TCP_PACKET = 0x21;
    public static final byte TCP_DISCONNECT = 0x41;

    public static final byte UDP_ASSOCIATE = 0x02;
    public static final byte UDP_PACKET = 0x22;
    public static final byte UDP_DISSOCIATE = 0x42;

    // TCP connection status
    public static final byte TCP_CONNECTION_OK = 0;
    public static final byte TCP_CONNECTION_GENERAL_FAIL = 1;
    public static final byte TCP_CONNECTION_REFUSED = 2;
    public static final byte TCP_CONNECTION_TIMEOUT = 3;
    public static final byte TCP_CONNECTION_UNREACHABLE = 4;
    public static final byte TCP_CONNECTION_UNKNOWN_HOST = 5;
    public static final byte TCP_CONNECTION_LIMIT_EXCEED = 6;

    public static final byte TCP_DISCONNECT_GENERAL = 1;
    public static final byte TCP_DISCONNECT_CLIENT = 2;
    public static final byte TCP_DISCONNECT_BROKEN_PIPE = 3;
    public static final byte TCP_DISCONNECT_CONNECTION_RESET = 4;

    public static final int BUFFER_SIZE = 2048;
    public static final int MAX_CONNECTIONS = 1024;
    public static final int MAX_QUEUE = 16;

    // Crypto
    public static final String RSA_ALGORITHM = "RSA";
    public static final String AES_ALGORITHM = "AES";
    public static final int RSA_KEYSIZE = 2048;
    public static final int RSA_MODULUSSIZE_BYTES = RSA_KEYSIZE / 8;
    public static final int RSA_PUBLICKEYSIZE_BYTES = 294;
    public static final int RSA_PRIVATEKEYSIZE_BYTES = 1219;
    public static final int AES_KEYSIZE = 128;
    public static final int AES_KEYSIZE_BYTES = AES_KEYSIZE / 8;

    // Hash
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int HASH_SIZE = 32;
    public static final int SECRET_SALT_SIZE = 256;

    public static final String CHARSET = "UTF-8";
}
