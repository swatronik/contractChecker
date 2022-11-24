package src.config;

import java.util.HashMap;

public class ConnectionConfig {

    private static final HashMap<String, String> connections = new HashMap<>();

    public static String getConnectionUrl(String connectionName) {
        return connections.get(connectionName);
    }

    public static HashMap<String, String> getConnections() {
        return connections;
    }

    public static void setConnection(String connectionName, String connectionUrl) {
        connections.put(connectionName, connectionUrl);
    }

    public static Boolean isExist(String connectionName) {
        return connections.containsKey(connectionName);
    }

    public static void deleteConnection(String connectionName) {
        connections.remove(connectionName);
    }

    public static void clear() {
        connections.clear();
    }
}
