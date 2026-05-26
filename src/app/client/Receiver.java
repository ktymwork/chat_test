package app.client;

import app.message.BroadcastMessage;
import app.message.Message;
import app.message.WatchdogMessage;

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
                    // \r — перетираем строку с ">>> " чтобы сообщение вышло чисто
                    System.out.println("\r[" + FMT.format(bm.getTimestamp()) + "] "
                            + bm.getUsername() + ": " + bm.getText());
                    System.out.print(">>> ");
                } else if (msg instanceof WatchdogMessage wm) {
                    System.out.println("\r[server] participants online: " + wm.getParticipantCount());
                    System.out.print(">>> ");
                }
            }
        } catch (Exception e) {
            System.out.println("\n[disconnected from server]");
            System.exit(0);
        }
    }
}