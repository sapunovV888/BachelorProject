package project.bachelor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnector {

    private static final String CONFIG_FILE = "db_config.txt";

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Map<String, String> config = new HashMap<>();
        File file = new File(CONFIG_FILE);

        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Looking for file: " + new File("db_config.txt").getAbsolutePath());


        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || !line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                config.put(parts[0].trim(), parts[1].trim());
            }

            url = config.get("url");
            user = config.get("user");
            password = config.get("password");

            if (url == null || user == null || password == null) {
                throw new RuntimeException("Конфігураційний файл неповний або некоректний.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Не вдалося прочитати файл конфігурації бази даних.", e);
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
