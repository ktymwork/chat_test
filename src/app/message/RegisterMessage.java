package app.message;

public class RegisterMessage extends Message {
    private final String username;

    public RegisterMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
