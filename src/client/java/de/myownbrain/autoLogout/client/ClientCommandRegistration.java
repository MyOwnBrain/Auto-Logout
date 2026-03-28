package de.myownbrain.autoLogout.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientCommandRegistration {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommands.literal("auto-logout")
                            .then(ClientCommands.literal("enable")
                                    .executes(ctx -> toggleAutoLogout(true)))
                            .then(ClientCommands.literal("disable")
                                    .executes(ctx -> toggleAutoLogout(false)))
                            .then(ClientCommands.literal("threshold")
                                    .executes(ctx -> showThreshold(ctx.getSource()))
                                    .then(ClientCommands.argument("value", IntegerArgumentType.integer(0, 20))
                                            .executes(ctx -> setThreshold(IntegerArgumentType.getInteger(ctx, "value")))
                                    )
                            )
                            .then(ClientCommands.literal("entity-tracking")
                                    .then(ClientCommands.literal("enable")
                                            .executes(ctx -> toggleEntityTracking(true)))
                                    .then(ClientCommands.literal("disable")
                                            .executes(ctx -> toggleEntityTracking(false)))
                                    .then(ClientCommands.literal("entity-count")
                                            .executes(ctx -> showNearbyEntityCount(ctx.getSource()))
                                            .then(ClientCommands.argument("value", IntegerArgumentType.integer(1, 10))
                                                    .executes(ctx -> setNearbyEntityCount(IntegerArgumentType.getInteger(ctx, "value")))))
                                    .then(ClientCommands.literal("radius")
                                            .executes(ctx -> showTrackingRadius(ctx.getSource()))
                                            .then(ClientCommands.argument("value", DoubleArgumentType.doubleArg(1.0, 64.0))
                                                    .executes(ctx -> setTrackingRadius(DoubleArgumentType.getDouble(ctx, "value")))))
                            )
                            .then(ClientCommands.literal("help")
                                    .executes(ctx -> showHelp(ctx.getSource()))
                            )
                            .then(ClientCommands.literal("join-message")
                                    .then(ClientCommands.literal("enable")
                                            .executes(ctx -> toggleJoinMessage(true)))
                                    .then(ClientCommands.literal("disable")
                                            .executes(ctx -> toggleJoinMessage(false)))
                            )
            );
        });
    }

    private static int toggleAutoLogout(boolean enable) {
        if (Minecraft.getInstance().player == null) return 0;

        ConfigManager.isModEnabled = enable;
        ConfigManager.saveConfig();
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Auto Logout ").append(Component.literal(enable ? "enabled" : "disabled").withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(enable ? ChatFormatting.GREEN : ChatFormatting.RED)));
        return Command.SINGLE_SUCCESS;
    }

    private static int showThreshold(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("Health threshold set to ").append(Component.literal(String.valueOf(ConfigManager.healthThreshold)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setThreshold(int threshold) {
        if (Minecraft.getInstance().player == null) return 0;

        if (threshold >= 0 && threshold <= 20) {
            ConfigManager.healthThreshold = threshold;
            ConfigManager.saveConfig();
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Health threshold set to ").append(Component.literal(String.valueOf(ConfigManager.healthThreshold)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        } else {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("Invalid threshold! Must be between 0 and 20.").withStyle(style -> style.withColor(ChatFormatting.RED))
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleEntityTracking(boolean enable) {
        if (Minecraft.getInstance().player == null) return 0;
        ConfigManager.isEntityTrackingEnabled = enable;
        ConfigManager.saveConfig();
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Entity Tracking ").append(Component.literal(enable ? "enabled" : "disabled").withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(enable ? ChatFormatting.GREEN : ChatFormatting.RED)));
        return Command.SINGLE_SUCCESS;
    }

    private static int showNearbyEntityCount(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("Nearby Entity Count set to ").append(Component.literal(String.valueOf(ConfigManager.nearbyEntityCount)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setNearbyEntityCount(int count) {
        if (Minecraft.getInstance().player == null) return 0;

        if (count >= 1 && count <= 10) {
            ConfigManager.nearbyEntityCount = count;
            ConfigManager.saveConfig();
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Nearby Entity Count set to ").append(Component.literal(String.valueOf(ConfigManager.nearbyEntityCount)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        } else {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("Invalid Nearby Entity Count! Must be between 1 and 10.").withStyle(style -> style.withColor(ChatFormatting.RED))
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int showTrackingRadius(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("Tracking Radius set to ").append(Component.literal(String.valueOf(ConfigManager.radius)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setTrackingRadius(double radius) {
        if (Minecraft.getInstance().player == null) return 0;

        if (radius >= 1.0 && radius <= 64.0) {
            ConfigManager.radius = radius;
            ConfigManager.saveConfig();
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Tracking Radius set to ").append(Component.literal(String.valueOf(ConfigManager.radius)).withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(ChatFormatting.GOLD)));
        } else {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("Invalid Tracking Radius! Must be between 1 and 64.").withStyle(style -> style.withColor(ChatFormatting.RED))
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleJoinMessage(boolean enable) {
        if (Minecraft.getInstance().player == null) return 0;

        ConfigManager.showJoinMessage = enable;
        ConfigManager.saveConfig();
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Join Message ").append(Component.literal(enable ? "enabled" : "disabled").withStyle(style -> style.withBold(true))).withStyle(style -> style.withColor(enable ? ChatFormatting.GREEN : ChatFormatting.RED)));
        return Command.SINGLE_SUCCESS;
    }

    private static int showHelp(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("")
                .append(Component.literal("\nAuto Logout Commands\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                .append(Component.literal("/auto-logout help").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" - Displays the this help Menu\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout enable").withStyle(ChatFormatting.GREEN)).append(Component.literal(" - Enables the mod\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout disable").withStyle(ChatFormatting.RED)).append(Component.literal(" - Disables the mod\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout threshold").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal(" - Displays the current threshold\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout threshold <value>").withStyle(ChatFormatting.AQUA)).append(Component.literal(" - Sets the threshold\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout join-message enable").withStyle(ChatFormatting.GREEN)).append(Component.literal(" - Enables the message when joining a world\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout join-message disable").withStyle(ChatFormatting.RED)).append(Component.literal(" - Disables the message when joining a world\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nEntity Tracking:\n").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("/auto-logout entity-tracking enable").withStyle(ChatFormatting.GREEN)).append(Component.literal(" - Enables Entity Tracking\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout entity-tracking disable").withStyle(ChatFormatting.RED)).append(Component.literal(" - Disables Entity Tracking\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout entity-tracking entity-count").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal(" - Displays the current Nearby Entity Count\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout entity-tracking entity-count <value>").withStyle(ChatFormatting.AQUA)).append(Component.literal(" - Sets the Nearby Entity Count\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout entity-tracking radius").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal(" - Displays the current Tracking Radius\n").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("/auto-logout entity-tracking radius <value>").withStyle(ChatFormatting.AQUA)).append(Component.literal(" - Sets the Tracking Radius\n").withStyle(ChatFormatting.WHITE))
        );

        return Command.SINGLE_SUCCESS;
    }
}
