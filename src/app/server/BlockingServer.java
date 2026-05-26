package app.server;

import app.Settings;
import app.message.BroadcastMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

public class BlockingServer {
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Deque<BroadcastMessage> history = new ArrayDeque<>();
    private static final int HISTORY_SIZE = 10;

    public boolean isUsernameTaken(String username) {
        return clients.stream().anyMatch(c -> username.equalsIgnoreCase(c.getUsername()));
    }

    public void register(ClientHandler handler) {
        clients.add(handler);
    }

    public void unregister(ClientHandler handler) {
        clients.remove(handler);
    }

    public int clientCount() {
        return clients.size();
    }

    public void broadcast(BroadcastMessage message) {
        synchronized (history) {
            if (history.size() >= HISTORY_SIZE) history.pollFirst();
            history.addLast(message);
        }
        for (var client : clients) {
            client.send(message);
        }
    }

    public List<BroadcastMessage> getHistory() {
        synchronized (history) {
            return new ArrayList<>(history);
        }
    }

    public void start() throws IOException {
        var pool = Executors.newCachedThreadPool();
        try (var ss = new ServerSocket(Settings.getPort())) {
            System.out.println("Blocking chat server on port " + Settings.getPort());
            while (true) {
                var socket = ss.accept();
                System.out.println("New connection from " + socket.getRemoteSocketAddress());
                pool.submit(new ClientHandler(socket, this));
            }
        }
    }
}
