package tk.jackyliao123.proxy;

import java.io.UnsupportedEncodingException;

public class Util {
    public static int b2ub(byte b) {
        return b & 0xFF;
    }

    public static int bs2us(byte[] b) {
        return bs2us(b, 0);
    }

    public static int bs2us(byte[] b, int offset) {
        return ((b[offset] & 0xFF) << 8) + (b[offset + 1] & 0xFF);
    }

    public static byte[] us2bs(int n) {
        if (n >= 65536) {
            Logger.error("unsigned short out of range: " + n);
        }
        return new byte[]{(byte) (n >>> 8), (byte) (n & 0xFF)};
    }

    public static String bs2str(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) {
            String s = Integer.toHexString(aB & 0xFF).toUpperCase();
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static String dbs2str(byte[] b) {
        if (Logger.getLoggingLevel() <= Logger.DEBUG) {
            return bs2str(b);
        }
        return "";
    }

    public static boolean bseq(byte[] b1, byte[] b2) {
        return bseq(b1, 0, b1.length, b2, 0, b2.length);
    }

    public static boolean bseq(byte[] b1, int off1, int len1, byte[] b2, int off2, int len2) {
        if (len1 != len2) {
            return false;
        }
        for (int i = 0; i < len1; ++i) {
            if (b1[i + off1] != b2[i + off2]) {
                return false;
            }
        }
        return true;
    }


    public static String addr2str(byte type, byte[] addr) {
        if (type == Constants.DNS) {
            try {
                return new String(addr, Constants.CHARSET);
            } catch (UnsupportedEncodingException e) {
                return new String(addr);//TODO TODO TODO FIX ALL STRING WITH THIS ERROR
            }
        } else if (type == Constants.IPv4) {
            return b2ub(addr[0]) + "." + b2ub(addr[1]) + "." + b2ub(addr[2]) + "." + b2ub(addr[3]);
        } else if (type == Constants.IPv6) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < addr.length; i += 2) {
                builder.append(Integer.toHexString(b2ub(addr[i]) << 8 + b2ub(addr[i + 1])));
                if (i < addr.length - 2) {
                    builder.append(":");
                }
            }
            return builder.toString();
        }
        return "";
    }
}
