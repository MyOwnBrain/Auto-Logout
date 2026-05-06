package de.myownbrain.autoLogout.client;

import net.minecraft.client.Minecraft;

public class HealthMonitor {
    public static void monitorPlayerHealth() {
        Minecraft client = Minecraft.getInstance();

        if (ConfigManager.isModEnabled && ConfigManager.isHealthTrackingEnabled && client.player != null) {
            float health = client.player.getHealth();
            if (health <= ConfigManager.healthThreshold) {
                client.player.connection.getConnection().disconnect(LogoutScreen.createHealthScreen());
                ConfigManager.isHealthTrackingEnabled = false;
                ConfigManager.saveConfig();
            }
        }
    }
}
