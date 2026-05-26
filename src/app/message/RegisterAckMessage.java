package app.message;

public class RegisterAckMessage extends Message {
    private final boolean success;
    private final String errorText;

    public RegisterAckMessage(boolean success, String errorText) {
        this.success = success;
        this.errorText = errorText;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorText() {
        return errorText;
    }
}