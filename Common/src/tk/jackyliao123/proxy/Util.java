package tk.jackyliao123.proxy;

public class Util {
    public static int b2ub(byte b) {
        return b & 0xFF;
    }

    public static int bs2us(byte[] b) {
        return bs2us(b, 0);
    }

    public static int bs2us(byte[] b, int offset) {
        return (b[offset] << 8) + (b[offset + 1] & 0xFF);
    }

    public static String bs2str(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(Integer.toHexString(b[i] & 0xFF).toUpperCase());
        }
        return sb.toString();
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
}
