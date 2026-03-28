package de.myownbrain.autoLogout.client;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class HealthMonitor {
    static DecimalFormat healthFormat = new DecimalFormat("#.##");
    static DecimalFormat coordsFormat = new DecimalFormat("#");

    public static void monitorPlayerHealth(Minecraft client) {
        if (ConfigManager.isModEnabled && client.player != null) {
            float health = client.player.getHealth();
            if (health <= ConfigManager.healthThreshold) {
                String playerX = coordsFormat.format(client.player.getX() >= 0 ? Math.floor(client.player.getX()) : Math.ceil(client.player.getX()));
                String playerY = coordsFormat.format(client.player.getY() >= 0 ? Math.floor(client.player.getY()) : Math.ceil(client.player.getY()));
                String playerZ = coordsFormat.format(client.player.getZ() >= 0 ? Math.floor(client.player.getZ()) : Math.ceil(client.player.getZ()));

                MutableComponent text = Component.literal("You were disconnected by Auto Logout due to low health.\n\n").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GREEN)).append(Component.literal("Health: " + healthFormat.format(client.player.getHealth()) + " (≈" + Math.floor(client.player.getHealth()) / 2 + " Hearts)\n").withStyle(style -> style.withColor(ChatFormatting.GOLD))).append(Component.literal(String.format("Coordinates: %s %s %s", playerX, playerY, playerZ)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));

                if (ConfigManager.isEntityTrackingEnabled) {
                    List<String> entityNames = NearestEntityFinder.getNearestEntities().stream().map(entity -> entity instanceof Player ? entity.getName().getString() : entity.getType().getDescription().getString()).toList();
                    text.append(Component.literal("\nNearby Entities: " + String.join(", ", entityNames)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
                }

                text.append(Component.literal("\n\nAuto Logout got disabled.").withStyle(style -> style.withColor(ChatFormatting.WHITE)));

                client.player.connection.getConnection().disconnect(text);
                ConfigManager.isModEnabled = false;
                ConfigManager.saveConfig();
            }
        }
    }
}
