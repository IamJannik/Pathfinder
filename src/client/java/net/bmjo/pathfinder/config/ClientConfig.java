package net.bmjo.pathfinder.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Config for client-side settings, stored in a properties file.
 *
 * @author BMJO
 * @version 1.0
 */
public class ClientConfig {
    private final HashMap<String, Boolean> config = new HashMap<>();
    private final File file;
    private boolean broken = false;

    /**
     * Constructs a new instance of the ClientConfig class.
     *
     * @param filename The name of the configuration file (excluding extension).
     */
    public ClientConfig(String filename) {
        Path path = FabricLoader.getInstance().getConfigDir();
        this.file = path.resolve(filename + ".properties").toFile();
        if (!file.exists()) {
            try {
                createConfig();
            } catch (IOException e) {
                broken = true;
            }
        }

        if (!broken) {
            try {
                loadConfig();
            } catch (Exception e) {
                broken = true;
            }
        }
    }

    private void createConfig() throws IOException {
        if (this.file.getParentFile().mkdirs())
            Files.createFile(this.file.toPath());
    }

    private void loadConfig() throws IOException {
        Scanner reader = new Scanner(this.file);
        for (int line = 1; reader.hasNextLine(); line++) {
            parseConfigEntry(reader.nextLine(), line);
        }
    }

    private void saveConfig() throws IOException {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : config.entrySet()) {
            content.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        PrintWriter writer = new PrintWriter(this.file, StandardCharsets.UTF_8);
        writer.write(content.toString());
        writer.close();
    }

    private void parseConfigEntry(String entry, int line) {
        if (!entry.isEmpty() && !entry.startsWith("#")) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                config.put(parts[0], parts[1].equalsIgnoreCase("true"));
            } else {
                throw new RuntimeException("Syntax error in config file on line " + line + "!");
            }
        }
    }

    /**
     * Retrieves the boolean value associated with the specified key or returns the default value if not found.
     *
     * @param key The key to look up in the configuration.
     * @param def The default value to return if the key is not present.
     * @return The boolean value associated with the key, or the default value if not found.
     */
    public boolean getOrDefault(String key, boolean def) {
        if (!config.containsKey(key)) {
            this.set(key, def);
            try {
                saveConfig();
            } catch (IOException e) {
                broken = true;
            }
        }
        return config.getOrDefault(key, def);
    }

    /**
     * Sets the boolean value associated with the specified key and saves the configuration.
     *
     * @param key The key to set in the configuration.
     * @param val The boolean value to associate with the key.
     */
    public void set(String key, boolean val) {
        config.put(key, val);
        try {
            saveConfig();
        } catch (IOException e) {
            broken = true;
        }
    }
}

