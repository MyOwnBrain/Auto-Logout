package de.myownbrain.autoLogout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AutoLogoutClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Auto Logout Client Mod Initialized!");
        ConfigManager.loadConfig();
        ClientCommandRegistration.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (ConfigManager.isModEnabled) {
                HealthMonitor.monitorPlayerHealth(client);

                if (ConfigManager.isEntityTrackingEnabled)
                    NearestEntityFinder.updateNearestEntities(client, ConfigManager.radius);
            }

            if (ModMenuIntegration.isAvailable()) ModMenuIntegration.handleToggleKey(client);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null && ConfigManager.showJoinMessage) {
                client.player.sendSystemMessage(Component.literal("Auto Logout is ").withStyle(ConfigManager.isModEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .append(Component.literal(ConfigManager.isModEnabled ? "enabled " : "disabled ").withStyle(ConfigManager.isModEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                        .append(Component.literal("(threshold ").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(String.valueOf(ConfigManager.healthThreshold)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append(Component.literal(")").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("\nEntity Tracking is ").withStyle(ConfigManager.isEntityTrackingEnabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                        .append(Component.literal(ConfigManager.isEntityTrackingEnabled ? "enabled " : "disabled ").withStyle(ConfigManager.isEntityTrackingEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD)));
            }
        });
    }
}
