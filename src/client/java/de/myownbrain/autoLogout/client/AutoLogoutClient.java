package de.myownbrain.autoLogout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;

public class AutoLogoutClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Auto Logout Client Mod Initialized!");
        ConfigManager.loadConfig();
        ClientCommandRegistration.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && ModMenuIntegration.currentKeyBinding != InputUtil.UNKNOWN_KEY) {
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

            monitorPlayerHealth(client);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Auto Logout is ")
                        .append(Text.literal(ConfigManager.isModEnabled ? "enabled " : "disabled ").styled(style -> style.withBold(true))).styled(style -> style.withColor(ConfigManager.isModEnabled ? Formatting.GREEN : Formatting.RED))
                        .append(Text.literal("with an threshold of ").styled(style -> style.withBold(false).withColor(Formatting.GOLD)))
                        .append(Text.literal(String.valueOf(ConfigManager.healthThreshold)).styled(style -> style.withBold(true).withColor(Formatting.GOLD))), false)
                ;
            }
        });
    }

    DecimalFormat healthFormat = new DecimalFormat("#.##");
    DecimalFormat coordsFormat = new DecimalFormat("#");

    private void monitorPlayerHealth(MinecraftClient client) {
        if (client.player != null && ConfigManager.isModEnabled) {
            String playerX = coordsFormat.format(client.player.getX() >= 0 ? Math.floor(client.player.getX()) : Math.ceil(client.player.getX()));
            String playerY = coordsFormat.format(client.player.getY() >= 0 ? Math.floor(client.player.getY()) : Math.ceil(client.player.getY()));
            String playerZ = coordsFormat.format(client.player.getZ() >= 0 ? Math.floor(client.player.getZ()) : Math.ceil(client.player.getZ()));
            float health = client.player.getHealth();
            if (health <= ConfigManager.healthThreshold) {
                client.player.networkHandler.getConnection().disconnect(
                        Text.literal("You were disconnected due to low health by Auto Logout.\n\n").styled(style -> style.withBold(true).withColor(Formatting.GREEN))
                                .append(Text.literal("Health: " + healthFormat.format(client.player.getHealth()) + "\n").styled(style -> style.withColor(Formatting.GOLD)))
                                .append(Text.literal(String.format("Coordinates: %s %s %s \n\n", playerX, playerY, playerZ)).styled(style -> style.withColor(Formatting.GOLD)))
                                .append(Text.literal("Auto Logout got disabled.").styled(style -> style.withColor(Formatting.WHITE)))
                );
                ConfigManager.isModEnabled = false;
                ConfigManager.saveConfig();
            }
        }
    }
}
