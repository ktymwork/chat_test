package app.client;

import app.message.BroadcastMessage;
import app.message.Message;

import java.io.ObjectInputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Receiver implements Runnable {
    private final ObjectInputStream in;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    public Receiver(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                var msg = (Message) in.readObject();
                if (msg instanceof BroadcastMessage bm) {
                    System.out.println("[" + FMT.format(bm.getTimestamp()) + "] "
                            + bm.getUsername() + ": " + bm.getText());
                }
            }
        } catch (Exception e) {
            System.out.println("\n[disconnected from server]");
            System.exit(0);
        }
    }
}
