package com.minintercom.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple utility to load environment variables from a .env file.
 */
public class EnvLoader {

    private static final Map<String, String> envMap = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        String rootPath = System.getProperty("user.dir");
        // Try to find .env in the project root or one level up (if running from
        // backend/)
        String[] paths = {
                Paths.get(rootPath, ".env").toString(),
                Paths.get(rootPath, "..", ".env").toString()
        };

        for (String path : paths) {
            if (Files.exists(Paths.get(path))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            continue;
                        int sep = line.indexOf('=');
                        if (sep > 0) {
                            String key = line.substring(0, sep).trim();
                            String value = line.substring(sep + 1).trim();
                            // Remove quotes if present
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            envMap.put(key, value);
                        }
                    }
                } catch (IOException e) {
                    // Ignore
                }
                break;
            }
        }
    }

    /**
     * Gets an environment variable, checking the .env file first, then the system
     * environment.
     */
    public static String get(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = envMap.get(key);
        }
        return value;
    }
}
