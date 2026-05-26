package app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = Settings.class.getClassLoader()
                .getResourceAsStream("chat.properties")) {
            if (in == null) throw new RuntimeException("chat.properties not found in classpath");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load chat.properties", e);
        }
    }

    public static String getHost()             { return props.getProperty("server.host", "localhost"); }
    public static int    getPort()             { return Integer.parseInt(props.getProperty("server.port", "7777")); }
    public static String getServerType()       { return props.getProperty("server.type", "blocking"); }
    public static int    getTimeoutMs()        { return Integer.parseInt(props.getProperty("server.timeout.ms", "30000")); }
    public static long   getKeepalivePeriodMs(){ return Long.parseLong(props.getProperty("server.keepalive.period.ms", "10000")); }
}
