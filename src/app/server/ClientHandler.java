package app.server;

import app.Settings;
import app.message.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BlockingServer server;
    private ObjectOutputStream out;
    private String username;

    public ClientHandler(Socket socket, BlockingServer server) {
        this.socket = socket;
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    // synchronized — защищает от одновременной записи из broadcast и watchdog потоков
    public synchronized void send(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            System.err.println("Failed to send to " + username + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(Settings.getTimeoutMs()); // таймаут простоя
            out = new ObjectOutputStream(socket.getOutputStream());
            var in = new ObjectInputStream(socket.getInputStream());

            // 1. Регистрация
            var reg = (RegisterMessage) in.readObject();
            username = reg.getUsername().strip();

            if (username.isBlank() || server.isUsernameTaken(username)) {
                send(new RegisterAckMessage(false,
                        username.isBlank() ? "username cannot be empty" : "username already taken"));
                return;
            }

            server.register(this);
            send(new RegisterAckMessage(true, null));
            System.out.println("[+] " + username + " joined. Online: " + server.clientCount());

            // 2. Основной цикл приёма сообщений
            while (true) {
                var msg = (Message) in.readObject();
                if (msg instanceof ChatMessage chat) {
                    server.broadcast(new BroadcastMessage(username, chat.getText(), Instant.now()));
                }
            }

        } catch (Exception e) {
            // Клиент отключился, таймаут, или ошибка — просто выходим
        } finally {
            server.unregister(this);
            if (username != null) {
                System.out.println("[-] " + username + " left. Online: " + server.clientCount());
            }
            try { socket.close(); } catch (Exception ignored) {}
        }
    }
}
