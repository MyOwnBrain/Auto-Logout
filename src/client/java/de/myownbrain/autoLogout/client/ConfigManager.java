package de.myownbrain.autoLogout.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/auto_logout.json");
    private static final File BACKUP_FILE = new File("config/auto_logout.json.bak");
    private static final int CONFIG_VERSION = 1;

    public static boolean isModEnabled = true;
    public static String keyBinding = "key.keyboard.unknown";
    public static InputConstants.Key currentKeyBinding = InputConstants.getKey(keyBinding);

    public static boolean isHealthTrackingEnabled = true;
    public static float healthThreshold = 4.0f;

    public static boolean isTPSTrackingEnabled = false;
    public static int tpsThreshold = 5;

    public static boolean isEntityScanningEnabled = true;
    public static int nearbyEntityCount = 5;
    public static double radius = 20;

    public static boolean showJoinMessage = true;

    public static void loadConfig() {
        boolean updated = false;

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                String configJSON = reader.readAllAsString();
                ConfigData config = configJSON.isBlank() ? null : GSON.fromJson(configJSON, ConfigData.class);

                if (config == null) updated = true; // empty file: keep the defaults and write them back below
                else {
                    int configVersion = config.configVersion == null ? 0 : config.configVersion;
                    if (configVersion < CONFIG_VERSION) {
                        config = migrateConfig(configVersion, configJSON);
                        updated = true;
                    }

                    if (config.isModEnabled != null) isModEnabled = config.isModEnabled;
                    else updated = true;

                    if (config.keyBinding != null && !config.keyBinding.isEmpty()) keyBinding = config.keyBinding;
                    else updated = true;

                    try {
                        currentKeyBinding = InputConstants.getKey(keyBinding);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unknown key '" + keyBinding + "', resetting the hotkey.");
                        keyBinding = "key.keyboard.unknown";
                        currentKeyBinding = InputConstants.UNKNOWN;
                        updated = true;
                    }

                    if (config.isHealthTrackingEnabled != null)
                        isHealthTrackingEnabled = config.isHealthTrackingEnabled;
                    else updated = true;

                    if (config.healthThreshold != null) healthThreshold = config.healthThreshold;
                    else updated = true;

                    if (config.isTPSTrackingEnabled != null) isTPSTrackingEnabled = config.isTPSTrackingEnabled;
                    else updated = true;

                    if (config.tpsThreshold != null) tpsThreshold = config.tpsThreshold;
                    else updated = true;

                    if (config.isEntityScanningEnabled != null)
                        isEntityScanningEnabled = config.isEntityScanningEnabled;
                    else updated = true;

                    if (config.nearbyEntityCount != null) nearbyEntityCount = config.nearbyEntityCount;
                    else updated = true;

                    if (config.radius != null) radius = config.radius;
                    else updated = true;

                    if (config.showJoinMessage != null) showJoinMessage = config.showJoinMessage;
                    else updated = true;
                }
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            } catch (JsonParseException e) {
                System.err.println("Config is invalid: " + e.getMessage());
                if (backupBrokenConfig()) updated = true; // only overwrite if the backup worked
            }
        } else updated = true;

        if (updated) saveConfig(); // write back with missing defaults filled in
    }

    public static void saveConfig() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                ConfigData config = new ConfigData(CONFIG_VERSION, isModEnabled, currentKeyBinding != null ? currentKeyBinding.getName() : "key.keyboard.unknown", isHealthTrackingEnabled, healthThreshold, isTPSTrackingEnabled, tpsThreshold, isEntityScanningEnabled, nearbyEntityCount, radius, showJoinMessage);
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static boolean backupBrokenConfig() {
        try {
            Files.copy(CONFIG_FILE.toPath(), BACKUP_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Config file is invalid. A copy was saved to " + BACKUP_FILE.getPath() + " and the defaults were restored.");
            return true;
        } catch (IOException e) {
            System.err.println("Config file is invalid and the backup failed, leaving the file untouched: " + e.getMessage());
            return false;
        }
    }

    private static class ConfigData {
        Integer configVersion;

        Boolean isModEnabled;
        String keyBinding;

        Boolean isHealthTrackingEnabled;
        Float healthThreshold;

        Boolean isTPSTrackingEnabled;
        Integer tpsThreshold;

        Boolean isEntityScanningEnabled;
        Integer nearbyEntityCount;
        Double radius;

        Boolean showJoinMessage;

        public ConfigData(Integer configVersion, Boolean isModEnabled, String keyBinding, Boolean isHealthTrackingEnabled, Float healthThreshold, Boolean isTPSTrackingEnabled, Integer tpsThreshold, Boolean isEntityScanningEnabled, Integer nearbyEntityCount, Double radius, Boolean showJoinMessage) {
            this.configVersion = configVersion;
            this.isModEnabled = isModEnabled;
            this.keyBinding = keyBinding;
            this.isHealthTrackingEnabled = isHealthTrackingEnabled;
            this.healthThreshold = healthThreshold;
            this.isTPSTrackingEnabled = isTPSTrackingEnabled;
            this.tpsThreshold = tpsThreshold;
            this.isEntityScanningEnabled = isEntityScanningEnabled;
            this.nearbyEntityCount = nearbyEntityCount;
            this.radius = radius;
            this.showJoinMessage = showJoinMessage;
        }
    }

    private static ConfigData migrateConfig(int configVersion, String configJSON) {
        List<Function<String, String>> migrations = new ArrayList<>();
        migrations.add(ConfigManager::migrateConfigV0toV1);

        String newConfigJSONString = configJSON;
        for (Function<String, String> migration : migrations.subList(configVersion, migrations.size()))
            newConfigJSONString = migration.apply(newConfigJSONString);

        ConfigData newConfig = GSON.fromJson(newConfigJSONString, ConfigData.class);
        newConfig.configVersion = CONFIG_VERSION;
        return newConfig;
    }

    private static String migrateConfigV0toV1(String configJson) {
        return configJson.replace("isEntityTrackingEnabled", "isEntityScanningEnabled");
    }
}
