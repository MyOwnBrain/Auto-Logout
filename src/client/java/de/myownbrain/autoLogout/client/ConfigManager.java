package de.myownbrain.autoLogout.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;

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
    public static InputConstants.Key currentKeyBinding = InputConstants.getKey(keyBinding);

    public static boolean isEntityTrackingEnabled = true;
    public static int nearbyEntityCount = 5;
    public static double radius = 20;

    public static boolean showJoinMessage = true;

    public static void loadConfig() {
        boolean updated = false;

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData config = GSON.fromJson(reader, ConfigData.class);

                if (config.isModEnabled != null) isModEnabled = config.isModEnabled;
                else updated = true;

                if (config.healthThreshold != null) healthThreshold = config.healthThreshold;
                else updated = true;

                if (config.keyBinding != null && !config.keyBinding.isEmpty())
                    keyBinding = config.keyBinding;
                else updated = true;

                currentKeyBinding = InputConstants.getKey(keyBinding);

                if (config.isEntityTrackingEnabled != null)
                    isEntityTrackingEnabled = config.isEntityTrackingEnabled;
                else updated = true;

                if (config.nearbyEntityCount != null)
                    nearbyEntityCount = config.nearbyEntityCount;
                else updated = true;

                if (config.radius != null)
                    radius = config.radius;
                else updated = true;

                if (config.showJoinMessage != null)
                    showJoinMessage = config.showJoinMessage;
                else updated = true;

            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        } else {
            updated = true;
        }

        if (updated) {
            saveConfig(); // write back with missing defaults filled in
        }
    }

    public static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                ConfigData config = new ConfigData(isModEnabled, healthThreshold, currentKeyBinding != null ? currentKeyBinding.getName() : "key.keyboard.unknown", isEntityTrackingEnabled, nearbyEntityCount, radius, showJoinMessage);
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static class ConfigData {
        Boolean isModEnabled;
        Float healthThreshold;
        String keyBinding;

        Boolean isEntityTrackingEnabled;
        Integer nearbyEntityCount;
        Double radius;

        Boolean showJoinMessage;

        public ConfigData(Boolean isModEnabled, Float healthThreshold, String keyBinding, Boolean isEntityTrackingEnabled, Integer nearbyEntityCount, Double radius, Boolean showJoinMessage) {
            this.isModEnabled = isModEnabled;
            this.healthThreshold = healthThreshold;
            this.keyBinding = keyBinding;
            this.isEntityTrackingEnabled = isEntityTrackingEnabled;
            this.nearbyEntityCount = nearbyEntityCount;
            this.radius = radius;
            this.showJoinMessage = showJoinMessage;
        }
    }
}
