package app.server;

import app.Settings;
import app.message.BroadcastMessage;
import app.message.WatchdogMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BlockingServer {

    private final List<app.server.ClientHandler> clients = new CopyOnWriteArrayList<>();

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
        for (var client : clients) {
            client.send(message);
        }
    }

    private void startWatchdog() {
        var scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "watchdog");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            int count = clients.size();
            if (count == 0) return;
            var msg = new WatchdogMessage(count);
            for (var client : clients) {
                client.send(msg);
            }
        }, Settings.getWatchdogPeriodMs(), Settings.getWatchdogPeriodMs(), TimeUnit.MILLISECONDS);
    }

    public void start() throws IOException {
        startWatchdog();
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
