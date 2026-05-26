package app.client;

import app.Settings;
import app.message.ChatMessage;
import app.message.PingMessage;
import app.message.RegisterAckMessage;
import app.message.RegisterMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        var scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        var username = scanner.nextLine().strip();

        var socket = new Socket(Settings.getHost(), Settings.getPort());
        socket.setSoTimeout(0);

        var out = new ObjectOutputStream(socket.getOutputStream());
        var in  = new ObjectInputStream(socket.getInputStream());

        out.writeObject(new RegisterMessage(username));
        out.flush();

        var ack = (RegisterAckMessage) in.readObject();
        if (!ack.isSuccess()) {
            System.out.println("Registration failed: " + ack.getErrorText());
            socket.close();
            return;
        }
        System.out.println("Joined as '" + username + "'. Type messages below, 'exit' to quit.");

        var receiver = new Thread(new Receiver(in), "receiver");
        receiver.setDaemon(true);
        receiver.start();

        var keepalive = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(Settings.getKeepalivePeriodMs() / 2);
                    synchronized (out) {
                        out.writeObject(new PingMessage());
                        out.flush();
                    }
                }
            } catch (Exception ignored) {}
        }, "keepalive");
        keepalive.setDaemon(true);
        keepalive.start();

        while (true) {
            var text = scanner.nextLine();
            if ("exit".equalsIgnoreCase(text.strip())) break;
            if (!text.isBlank()) {
                synchronized (out) {
                    out.writeObject(new ChatMessage(text));
                    out.flush();
                }
            }
        }

        socket.close();
        System.out.println("Bye!");
    }
}
