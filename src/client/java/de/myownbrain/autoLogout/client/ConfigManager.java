package de.myownbrain.autoLogout.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.util.InputUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/auto_logout.json");

    public static boolean isModEnabled = true;
    public static float healthThreshold = 4.0f;

    public static String keyBinding = "key.keyboard.unknown";

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData config = GSON.fromJson(reader, ConfigData.class);
                isModEnabled = config.isModEnabled;
                healthThreshold = config.healthThreshold;
                keyBinding = (config.keyBinding != null && !config.keyBinding.isEmpty()) ? config.keyBinding : "key.keyboard.unknown";
                ModMenuIntegration.currentKeyBinding = InputUtil.fromTranslationKey(keyBinding);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        } else {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                ConfigData config = new ConfigData(isModEnabled, healthThreshold, keyBinding != null ? ModMenuIntegration.currentKeyBinding.getTranslationKey() : "key.keyboard.unknown");
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static class ConfigData {
        boolean isModEnabled;
        float healthThreshold;
        String keyBinding;

        public ConfigData(boolean isModEnabled, float healthThreshold, String keyBinding) {
            this.isModEnabled = isModEnabled;
            this.healthThreshold = healthThreshold;
            this.keyBinding = keyBinding;
        }
    }
}
