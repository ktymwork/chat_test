package app.message;

import java.time.Instant;

public class BroadcastMessage extends Message {
    private final String username;
    private final String text;
    private final Instant timestamp;

    public BroadcastMessage(String username, String text, Instant timestamp) {
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUsername()   { return username; }
    public String getText()       { return text; }
    public Instant getTimestamp() { return timestamp; }
}
