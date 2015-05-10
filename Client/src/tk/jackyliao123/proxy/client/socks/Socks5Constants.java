package tk.jackyliao123.proxy.client.socks;

public class Socks5Constants {
    public static final byte VERSION = 0x05;

    // Authentication methods
    public static final byte AUTHMETHOD_NONE = 0x00;
    public static final byte AUTHMETHOD_GSSAPI = 0x01;
    public static final byte AUTHMETHOD_USERPASS = 0x02;
    public static final byte AUTHMETHOD_NOACCEPTABLE = (byte) 0xFF;
}
