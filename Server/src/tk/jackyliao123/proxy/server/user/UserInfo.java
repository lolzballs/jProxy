package tk.jackyliao123.proxy.server.user;

public class UserInfo {
    public final String username;
    public final byte[] salt;
    public final byte[] hash;

    public UserInfo(String username, byte[] salt, byte[] hash) {
        this.username = username;
        this.salt = salt;
        this.hash = hash;
    }
}
