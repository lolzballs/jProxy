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

    public static String toString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            sb.append(Integer.toHexString(b[i] & 0xFF).toUpperCase());
        }
        return sb.toString();
    }
}
