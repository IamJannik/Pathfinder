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

public class ClientConfig {
    private final HashMap<String, Boolean> config = new HashMap<>();
    private final File file;
    private boolean broken = false;

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

    public void set(String key, boolean val) {
        config.put(key, val);
        try {
            saveConfig();
        } catch (IOException e) {
            broken = true;
        }
    }
}

