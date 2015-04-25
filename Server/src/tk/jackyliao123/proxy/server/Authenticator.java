package tk.jackyliao123.proxy.server;

public class Authenticator {
    private final Validator validator;
    public Authenticator() {
        this.validator = new Validator(Variables.secretKeyFile);

    }
}
