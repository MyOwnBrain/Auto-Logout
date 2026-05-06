package de.myownbrain.autoLogout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class AutoLogoutClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Auto Logout Client Mod Initialized!");
        ConfigManager.loadConfig();
        ClientCommandRegistration.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (ModMenuIntegration.isAvailable()) ModMenuIntegration.handleToggleKey();
            if (!ConfigManager.isModEnabled) return;

            if (ConfigManager.isHealthTrackingEnabled) HealthMonitor.monitorPlayerHealth();
            if (ConfigManager.isTPSTrackingEnabled) TPSMonitor.monitorTPS();
            if (ConfigManager.isHealthTrackingEnabled && ConfigManager.isTPSTrackingEnabled && ConfigManager.isEntityTrackingEnabled)
                NearestEntityFinder.updateNearestEntities(ConfigManager.radius);
        });

        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            if (client.player != null && ConfigManager.showJoinMessage) {
                ArrayList<String> tracker = new ArrayList<>();
                if (ConfigManager.isHealthTrackingEnabled) tracker.add("Health");
                if (ConfigManager.isTPSTrackingEnabled) tracker.add("TPS");

                client.player.sendSystemMessage(Component.literal("Auto Logout is ").withStyle(ConfigManager.isModEnabled ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .append(Component.literal(ConfigManager.isModEnabled ? "enabled " : "disabled ").withStyle(ConfigManager.isModEnabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD))
                    .append(Component.literal(ConfigManager.isModEnabled ? ("→ " + (tracker.isEmpty() ? "No Trackers Enabled" : String.join(", ", tracker))) : "")));
            }
        });
    }
}
