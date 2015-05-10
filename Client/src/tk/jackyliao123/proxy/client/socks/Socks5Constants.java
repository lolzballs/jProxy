package tk.jackyliao123.proxy.client.socks;

public class Socks5Constants {
    public static final byte VERSION = 0x05;

    // Authentication methods
    public static final byte AUTHMETHOD_NONE = 0x00;
    public static final byte AUTHMETHOD_GSSAPI = 0x01;
    public static final byte AUTHMETHOD_USERPASS = 0x02;
    public static final byte AUTHMETHOD_NOACCEPTABLE = (byte) 0xFF;

    // Request commands
    public static final byte CMD_CONNECT = 0x01;
    public static final byte CMD_BIND = 0x02;
    public static final byte CMD_UDPASSOCIATE = 0x03;

    // AYTP
    public static final byte ATYP_IPv4 = 0x01;
    public static final byte ATYP_DOMAIN = 0x03;
    public static final byte ATYP_IPv6 = 0x04;

    // TCP Connection
    public static final byte REP_SUCCESS = 0x00;
    public static final byte REP_GENERALSOCKSFAILURE = 0x01;
    public static final byte REP_CONNECTIONNOTALLOWED = 0x02;
    public static final byte REP_HOSTUNREACHABLE = 0x03;
    public static final byte REP_CONNECTIONREFUSED = 0x05;
    public static final byte REP_TTLEXPIRED = 0x06;
    public static final byte REP_COMMANDNOTSUPPORTED = 0x07;
    public static final byte REP_ADDRESSTYPENOTSUPPORTED = 0x08;
}
