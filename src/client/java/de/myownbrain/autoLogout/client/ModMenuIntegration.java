package de.myownbrain.autoLogout.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    public static InputUtil.Key currentKeyBinding = InputUtil.UNKNOWN_KEY;

    public static boolean wasKeyPressed = false;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Auto Logout Configuration"));

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Auto Logout"), ConfigManager.isModEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> {
                    ConfigManager.isModEnabled = newValue;
                    ConfigManager.saveConfig();
                })
                .build()
        );

        general.addEntry(entryBuilder.startFloatField(Text.literal("Health threshold"), ConfigManager.healthThreshold)
                .setDefaultValue(4.0f)
                .setMin(0.0f)
                .setMax(20.0f)
                .setSaveConsumer(newValue -> {
                    ConfigManager.healthThreshold = newValue;
                    ConfigManager.saveConfig();
                })
                .build()
        );

        general.addEntry(entryBuilder.startKeyCodeField(Text.literal("Toggle Auto Logout"), currentKeyBinding)
                .setDefaultValue(InputUtil.UNKNOWN_KEY)
                .setKeySaveConsumer(newKey -> {
                    currentKeyBinding = newKey;
                    ConfigManager.keyBinding = newKey.getTranslationKey();
                    ConfigManager.saveConfig();
                })
                .build());

        builder.setSavingRunnable(ConfigManager::saveConfig);

        return builder.build();
    };

    public static boolean isToggleKeyPressed() {
        if (currentKeyBinding == null) {
            currentKeyBinding = InputUtil.UNKNOWN_KEY;
        }
        return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), currentKeyBinding.getCode());
    }
}
