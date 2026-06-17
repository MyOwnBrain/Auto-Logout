package de.myownbrain.autoLogout.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.text.DecimalFormat;
import java.util.List;

public class LogoutScreen {
    static DecimalFormat twoDecFormat = new DecimalFormat("#.##");
    static DecimalFormat intFormat = new DecimalFormat("#");

    public static MutableComponent createHealthScreen() {
        Minecraft client = Minecraft.getInstance();

        String playerX = intFormat.format(client.player.getX() >= 0 ? Math.floor(client.player.getX()) : Math.ceil(client.player.getX()));
        String playerY = intFormat.format(client.player.getY() >= 0 ? Math.floor(client.player.getY()) : Math.ceil(client.player.getY()));
        String playerZ = intFormat.format(client.player.getZ() >= 0 ? Math.floor(client.player.getZ()) : Math.ceil(client.player.getZ()));

        MutableComponent text = Component.literal("You were disconnected by Auto Logout due to low health.\n\n").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GREEN)).append(Component.literal("Health: " + twoDecFormat.format(client.player.getHealth()) + " (≈" + Math.floor(client.player.getHealth()) / 2 + " Hearts)\n").withStyle(style -> style.withColor(ChatFormatting.GOLD))).append(Component.literal(String.format("Coordinates: %s %s %s", playerX, playerY, playerZ)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));

        if (ConfigManager.isEntityScanningEnabled) {
            List<String> entityNames = NearestEntityFinder.getNearestEntities().stream().map(entity -> entity instanceof Player ? entity.getName().getString() : entity.getType().getDescription().getString()).toList();
            text.append(Component.literal("\nNearby Entities: " + String.join(", ", entityNames)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }

        text.append(Component.literal("\n\nHealth Tracking got disabled.").withStyle(style -> style.withColor(ChatFormatting.WHITE)));

        return text;
    }

    public static MutableComponent createTPSScreen() {
        Minecraft client = Minecraft.getInstance();

        String playerX = intFormat.format(client.player.getX() >= 0 ? Math.floor(client.player.getX()) : Math.ceil(client.player.getX()));
        String playerY = intFormat.format(client.player.getY() >= 0 ? Math.floor(client.player.getY()) : Math.ceil(client.player.getY()));
        String playerZ = intFormat.format(client.player.getZ() >= 0 ? Math.floor(client.player.getZ()) : Math.ceil(client.player.getZ()));

        MutableComponent text = Component.literal("You were disconnected by Auto Logout due to low TPS.\n\n").withStyle(style -> style.withBold(true).withColor(ChatFormatting.GREEN)).append(Component.literal("TPS: " + intFormat.format(TPSMonitor.getTps()) + " (" + twoDecFormat.format(TPSMonitor.getMspt()) + " MSPT)\n").withStyle(style -> style.withColor(ChatFormatting.GOLD))).append(Component.literal(String.format("Coordinates: %s %s %s", playerX, playerY, playerZ)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));

        if (ConfigManager.isEntityScanningEnabled) {
            List<String> entityNames = NearestEntityFinder.getNearestEntities().stream().map(entity -> entity instanceof Player ? entity.getName().getString() : entity.getType().getDescription().getString()).toList();
            text.append(Component.literal("\nNearby Entities: " + String.join(", ", entityNames)).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        }

        text.append(Component.literal("\n\nTPS Tracking got disabled.").withStyle(style -> style.withColor(ChatFormatting.WHITE)));

        return text;
    }
}
