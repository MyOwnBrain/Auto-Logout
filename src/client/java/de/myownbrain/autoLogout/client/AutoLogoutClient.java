package de.myownbrain.autoLogout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.util.InputUtil;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoLogoutClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Auto Logout Client Mod Initialized!");
        ConfigManager.loadConfig();
        ClientCommandRegistration.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (ConfigManager.isModEnabled) HealthMonitor.monitorPlayerHealth(client);

            if (ConfigManager.isEntityTrackingEnabled)
                NearestEntityFinder.updateNearestEntities(client, ConfigManager.radius);

            if (ModMenuIntegration.currentKeyBinding != InputUtil.UNKNOWN_KEY) {
                if (client.currentScreen != null) {
                    ModMenuIntegration.wasKeyPressed = false;
                    return;
                }

                boolean isKeyPressed = ModMenuIntegration.isToggleKeyPressed();

                if (isKeyPressed && !ModMenuIntegration.wasKeyPressed) {
                    ConfigManager.isModEnabled = !ConfigManager.isModEnabled;
                    ConfigManager.saveConfig();
                    client.player.sendMessage(Text.literal("Auto Logout ").append(Text.literal(ConfigManager.isModEnabled ? "enabled" : "disabled").styled(style -> style.withBold(true))).styled(style -> style.withColor(ConfigManager.isModEnabled ? Formatting.GREEN : Formatting.RED)), false);
                }

                ModMenuIntegration.wasKeyPressed = isKeyPressed;
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Auto Logout is ").formatted(ConfigManager.isModEnabled ? Formatting.GREEN : Formatting.RED)
                        .append(Text.literal(ConfigManager.isModEnabled ? "enabled " : "disabled ").formatted(ConfigManager.isModEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
                        .append(Text.literal("(threshold ").formatted(Formatting.GOLD))
                        .append(Text.literal(String.valueOf(ConfigManager.healthThreshold)).formatted(Formatting.GOLD, Formatting.BOLD))
                        .append(Text.literal(")").formatted(Formatting.GOLD))
                        .append(Text.literal("\nEntity Tracking is ").formatted(ConfigManager.isEntityTrackingEnabled ? Formatting.GREEN : Formatting.RED))
                        .append(Text.literal(ConfigManager.isEntityTrackingEnabled ? "enabled " : "disabled ").formatted(ConfigManager.isEntityTrackingEnabled ? Formatting.GREEN : Formatting.RED, Formatting.BOLD)), false)
                ;
            }
        });
    }
}
