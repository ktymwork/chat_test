package app.client;

import app.Settings;
import app.message.ChatMessage;
import app.message.RegisterAckMessage;
import app.message.RegisterMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
    public static void main(String[] args) throws Exception {
        var scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        var username = scanner.nextLine().strip();

        var socket = new Socket(Settings.getHost(), Settings.getPort());
        socket.setSoTimeout(0); // клиент не таймаутит — Receiver обработает разрыв сам

        var out = new ObjectOutputStream(socket.getOutputStream());
        var in  = new ObjectInputStream(socket.getInputStream());

        // Регистрация
        out.writeObject(new RegisterMessage(username));
        out.flush();

        var ack = (RegisterAckMessage) in.readObject();
        if (!ack.isSuccess()) {
            System.out.println("Registration failed: " + ack.getErrorText());
            socket.close();
            return;
        }
        System.out.println("Joined as '" + username + "'. Type messages below, 'exit' to quit.");
        System.out.print(">>> ");

        // Поток приёма сообщений от сервера
        var receiver = new Thread(new Receiver(in), "receiver");
        receiver.setDaemon(true);
        receiver.start();

        // Основной поток: читаем консоль и шлём сообщения
        while (scanner.hasNextLine()) {
            var text = scanner.nextLine();
            if ("exit".equalsIgnoreCase(text.strip())) break;
            if (!text.isBlank()) {
                out.writeObject(new ChatMessage(text));
                out.flush();
            }
            System.out.print(">>> ");
        }

        socket.close();
        System.out.println("Bye!");
    }
}
