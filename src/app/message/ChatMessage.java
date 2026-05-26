package app.message;

public class ChatMessage extends Message {
    private final String text;

    public ChatMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
