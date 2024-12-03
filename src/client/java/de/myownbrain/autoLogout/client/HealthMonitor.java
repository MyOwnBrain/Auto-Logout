package de.myownbrain.autoLogout.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.List;

public class HealthMonitor {
    static DecimalFormat healthFormat = new DecimalFormat("#.##");
    static DecimalFormat coordsFormat = new DecimalFormat("#");

    public static void monitorPlayerHealth(MinecraftClient client) {
        if (ConfigManager.isModEnabled) {
            float health = client.player.getHealth();
            if (health <= ConfigManager.healthThreshold) {
                String playerX = coordsFormat.format(client.player.getX() >= 0 ? Math.floor(client.player.getX()) : Math.ceil(client.player.getX()));
                String playerY = coordsFormat.format(client.player.getY() >= 0 ? Math.floor(client.player.getY()) : Math.ceil(client.player.getY()));
                String playerZ = coordsFormat.format(client.player.getZ() >= 0 ? Math.floor(client.player.getZ()) : Math.ceil(client.player.getZ()));

                MutableText text = Text.literal("You were disconnected due to low health by Auto Logout.\n\n").styled(style -> style.withBold(true).withColor(Formatting.GREEN))
                        .append(Text.literal("Health: " + healthFormat.format(client.player.getHealth()) + "\n").styled(style -> style.withColor(Formatting.GOLD)))
                        .append(Text.literal(String.format("Coordinates: %s %s %s", playerX, playerY, playerZ)).styled(style -> style.withColor(Formatting.GOLD)));

                if (ConfigManager.isEntityTrackingEnabled) {
                    List<String> entityNames = NearestEntityFinder.getNearestEntities().stream().map(entity -> entity.getName().getString()).toList();
                    text.append(Text.literal("\nNearby Entities: " + String.join(", ", entityNames)).styled(style -> style.withColor(Formatting.GOLD)));
                }

                text.append(Text.literal("\n\nAuto Logout got disabled.").styled(style -> style.withColor(Formatting.WHITE)));

                client.player.networkHandler.getConnection().disconnect(text);
                ConfigManager.isModEnabled = false;
                ConfigManager.saveConfig();
            }
        }
    }
}
