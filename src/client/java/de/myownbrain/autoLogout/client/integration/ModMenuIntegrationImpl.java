package de.myownbrain.autoLogout.client.integration;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.myownbrain.autoLogout.client.ConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegrationImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Auto Logout Configuration"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Auto Logout"), ConfigManager.isModEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> {
                ConfigManager.isModEnabled = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Whether Auto Logout is enabled or not in general."))
            .build()
        );

        general.addEntry(entryBuilder.startKeyCodeField(Component.literal("Toggle Auto Logout"), ConfigManager.currentKeyBinding)
            .setDefaultValue(InputConstants.UNKNOWN)
            .setKeySaveConsumer(newKey -> {
                ConfigManager.currentKeyBinding = newKey;
                ConfigManager.keyBinding = newKey.getName();
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Keybinding to toggle Auto Logout in general."))
            .setAllowMouse(false)
            .build());

        SubCategoryBuilder health = entryBuilder.startSubCategory(Component.literal("Health Tracking")).setExpanded(ConfigManager.isHealthTrackingEnabled);

        health.add(entryBuilder.startBooleanToggle(Component.literal("Enable Health Tracking"), ConfigManager.isHealthTrackingEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> {
                ConfigManager.isHealthTrackingEnabled = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Whether the player can be logged out due to low health."))
            .build());

        health.add(entryBuilder.startFloatField(Component.literal("Health threshold (2 Health = 1 Heart)"), ConfigManager.healthThreshold)
            .setDefaultValue(4.0f)
            .setMin(0.0f)
            .setMax(20.0f)
            .setSaveConsumer(newValue -> {
                ConfigManager.healthThreshold = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("When health drops below the threshold you get disconnected. (2 Health = 1 Heart)"))
            .build()
        );

        general.addEntry(health.build());

        SubCategoryBuilder tps = entryBuilder.startSubCategory(Component.literal("TPS Tracking")).setExpanded(ConfigManager.isTPSTrackingEnabled);

        tps.add(entryBuilder.startBooleanToggle(Component.literal("Enable TPS Tracking"), ConfigManager.isTPSTrackingEnabled)
            .setDefaultValue(false)
            .setSaveConsumer(newValue -> {
                ConfigManager.isTPSTrackingEnabled = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Whether the player can be logged out due to low TPS."))
            .build());

        tps.add(entryBuilder.startIntField(Component.literal("TPS threshold"), ConfigManager.tpsThreshold)
            .setDefaultValue(5)
            .setMin(0)
            .setMax(20)
            .setSaveConsumer(newValue -> {
                ConfigManager.tpsThreshold = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("When TPS drop below the threshold you get disconnected."))
            .build()
        );

        general.addEntry(tps.build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show nearby entities after disconnection"), ConfigManager.isEntityScanningEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> {
                ConfigManager.isEntityScanningEnabled = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Whether nearby entities should be displayed on the disconnect screen."))
            .build());

        general.addEntry(entryBuilder.startIntField(Component.literal("Nearby Entity Count"), ConfigManager.nearbyEntityCount)
            .setDefaultValue(5)
            .setMin(1)
            .setMax(10)
            .setSaveConsumer(newValue -> {
                ConfigManager.nearbyEntityCount = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Amount of the Nearby Entities displayed in the disconnect screen."))
            .build()
        );

        general.addEntry(entryBuilder.startDoubleField(Component.literal("Scanning Radius"), ConfigManager.radius)
            .setDefaultValue(20.0)
            .setMin(1.0)
            .setMax(64.0)
            .setSaveConsumer(newValue -> {
                ConfigManager.radius = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Radius to scan for entities."))
            .build()
        );

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Show message when joining a world"), ConfigManager.showJoinMessage)
            .setDefaultValue(true)
            .setSaveConsumer(newValue -> {
                ConfigManager.showJoinMessage = newValue;
                ConfigManager.saveConfig();
            })
            .setTooltip(Component.literal("Shows a message when joining a world telling whether the Mod and Entity Tracking are enabled."))
            .build()
        );

        builder.setSavingRunnable(ConfigManager::saveConfig);

        return builder.build();
    }

    public static boolean isToggleKeyPressed() {
        if (ConfigManager.currentKeyBinding == null) ConfigManager.currentKeyBinding = InputConstants.UNKNOWN;
        if (ConfigManager.currentKeyBinding == InputConstants.UNKNOWN
            || ConfigManager.currentKeyBinding.getValue() == InputConstants.UNKNOWN.getValue()
        ) return false;

        return InputConstants.isKeyDown(ConfigManager.currentKeyBinding.getValue());
    }
}
