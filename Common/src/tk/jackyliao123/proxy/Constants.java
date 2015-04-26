package tk.jackyliao123.proxy;

public class Constants {
    // Handshake Constants
    public static final byte[] MAGIC = new byte[]{'j', 'P', 'r', 'o', 'x', 'y'};
    public static final byte MAJOR = 2;
    public static final byte MINOR = 0;
    public static final int MAGIC_LENGTH = MAGIC.length;

    // Initalization return codes
    public static final byte INIT_SUCCESSFUL = 0;
    public static final byte INIT_UNSUPPORTEDVERSION = 1;

    // Login return codes
    public static final byte LOGIN_ACCEPTED = 0;
    public static final byte LOGIN_INVALIDKEY = 1;

    // Send packet
    public static final byte TYPE_PING = 0x00;
    public static final byte TYPE_TCP = 0x01;
    public static final byte TYPE_UDP = 0x02;

    //Connections
    public static final byte ICMP_PING = 0x00;

    public static final byte TCP_CONNECT = 0x01;
    public static final byte TCP_PACKET = 0x21;
    public static final byte TCP_DISCONNECT = 0x41;

    public static final byte UDP_ASSOCIATE = 0x02;
    public static final byte UDP_PACKET = 0x22;
    public static final byte UDP_DISSOCIATE = 0x42;


    // TCP connection status
    public static final byte TCP_CONNECTION_UNKNOWN = 2; // When status has not been recieved yet
    public static final byte TCP_CONNECTION_OK = 1;
    public static final byte TCP_CONNECTION_OTHER = 0;
    public static final byte TCP_CONNECTION_REFUSED = -1;
    public static final byte TCP_CONNECTION_TIMEOUT = -2;
    public static final byte TCP_CONNECTION_UNREACHABLE = -3;

    public static final int MAX_MTU = 1400;
    public static final int MAX_CONNECTIONS = 1024;

    // Crypto
    public static final String RSA_ALGORITHM = "RSA";
    public static final String AES_ALGORITHM = "AES";
    public static final int RSA_KEYSIZE = 2048;
    public static final int RSA_MODULUSSIZE_BYTES = RSA_KEYSIZE / 8;
    public static final int RSA_PUBLICKEYSIZE_BYTES = 294;
    public static final int AES_KEYSIZE = 256;
    public static final int AES_KEYSIZE_BYTES = AES_KEYSIZE / 8;

    // Hash
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int HASH_SIZE = 32;
    public static final int SECRET_SALT_SIZE = 256;

    public static final String CHARSET = "UTF-8";
}
