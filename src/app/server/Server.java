package app.server;

import app.Settings;

public class Server {
    public static void main(String[] args) throws Exception {
        var type = Settings.getServerType();
        System.out.println("Starting server, type: " + type);
        switch (type) {
            case "blocking" -> new BlockingServer().start();
            default -> throw new IllegalArgumentException("Unknown server type: " + type);
        }
    }
}
