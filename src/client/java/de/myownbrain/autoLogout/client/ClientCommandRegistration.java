package de.myownbrain.autoLogout.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClientCommandRegistration {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("auto-logout")
                            .then(ClientCommandManager.literal("enable")
                                    .executes(ctx -> toggleAutoLogout(true)))
                            .then(ClientCommandManager.literal("disable")
                                    .executes(ctx -> toggleAutoLogout(false)))
                            .then(ClientCommandManager.literal("threshold")
                                    .executes(ctx -> showThreshold(ctx.getSource()))
                                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 20))
                                            .executes(ctx -> setThreshold(IntegerArgumentType.getInteger(ctx, "value")))
                                    )
                            )
                            .then(ClientCommandManager.literal("entity-tracking")
                                    .then(ClientCommandManager.literal("enable")
                                            .executes(ctx -> toggleEntityTracking(true)))
                                    .then(ClientCommandManager.literal("disable")
                                            .executes(ctx -> toggleEntityTracking(false)))
                                    .then(ClientCommandManager.literal("entity-count")
                                            .executes(ctx -> showNearbyEntityCount(ctx.getSource()))
                                            .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 10))
                                                    .executes(ctx -> setNearbyEntityCount(IntegerArgumentType.getInteger(ctx, "value")))))
                                    .then(ClientCommandManager.literal("radius")
                                            .executes(ctx -> showTrackingRadius(ctx.getSource()))
                                            .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(1.0, 64.0))
                                                    .executes(ctx -> setTrackingRadius(DoubleArgumentType.getDouble(ctx, "value")))))
                            )
                            .then(ClientCommandManager.literal("help")
                                    .executes(ctx -> showHelp(ctx.getSource()))
                            )
            );
        });
    }

    private static int toggleAutoLogout(boolean enable) {
        ConfigManager.isModEnabled = enable;
        ConfigManager.saveConfig();
        MinecraftClient.getInstance().player.sendMessage(Text.literal("Auto Logout ").append(Text.literal(enable ? "enabled" : "disabled").styled(style -> style.withBold(true))).styled(style -> style.withColor(enable ? Formatting.GREEN : Formatting.RED)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showThreshold(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("Health threshold set to ").append(Text.literal(String.valueOf(ConfigManager.healthThreshold)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setThreshold(int threshold) {
        if (threshold >= 0 && threshold <= 20) {
            ConfigManager.healthThreshold = threshold;
            ConfigManager.saveConfig();
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Health threshold set to ").append(Text.literal(String.valueOf(ConfigManager.healthThreshold)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)), false);
            return Command.SINGLE_SUCCESS;
        } else {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("Invalid threshold! Must be between 0 and 20.").styled(style -> style.withColor(Formatting.RED)),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int toggleEntityTracking(boolean enable) {
        ConfigManager.isEntityTrackingEnabled = enable;
        ConfigManager.saveConfig();
        MinecraftClient.getInstance().player.sendMessage(Text.literal("Entity Tracking ").append(Text.literal(enable ? "enabled" : "disabled").styled(style -> style.withBold(true))).styled(style -> style.withColor(enable ? Formatting.GREEN : Formatting.RED)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showNearbyEntityCount(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("Nearby Entity Count set to ").append(Text.literal(String.valueOf(ConfigManager.nearbyEntityCount)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setNearbyEntityCount(int count) {
        if (count >= 1 && count <= 10) {
            ConfigManager.nearbyEntityCount = count;
            ConfigManager.saveConfig();
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Nearby Entity Count set to ").append(Text.literal(String.valueOf(ConfigManager.nearbyEntityCount)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)), false);
            return Command.SINGLE_SUCCESS;
        } else {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("Invalid Nearby Entity Count! Must be between 1 and 10.").styled(style -> style.withColor(Formatting.RED)),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int showTrackingRadius(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("Tracking Radius set to ").append(Text.literal(String.valueOf(ConfigManager.radius)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setTrackingRadius(double radius) {
        if (radius >= 1.0 && radius <= 64.0) {
            ConfigManager.radius = radius;
            ConfigManager.saveConfig();
            MinecraftClient.getInstance().player.sendMessage(Text.literal("Tracking Radius set to ").append(Text.literal(String.valueOf(ConfigManager.radius)).styled(style -> style.withBold(true))).styled(style -> style.withColor(Formatting.GOLD)), false);
            return Command.SINGLE_SUCCESS;
        } else {
            MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("Invalid Tracking Radius! Must be between 1 and 64.").styled(style -> style.withColor(Formatting.RED)),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int showHelp(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("")
                .append(Text.literal("\nAuto Logout Commands\n").formatted(Formatting.GOLD, Formatting.BOLD, Formatting.UNDERLINE))
                .append(Text.literal("/auto-logout help").formatted(Formatting.YELLOW)).append(Text.literal(" - Displays the this help Menu\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout enable").formatted(Formatting.GREEN)).append(Text.literal(" - Enables the mod\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout disable").formatted(Formatting.RED)).append(Text.literal(" - Disables the mod\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout threshold").formatted(Formatting.DARK_PURPLE)).append(Text.literal(" - Displays the current threshold\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout threshold <value>").formatted(Formatting.AQUA)).append(Text.literal(" - Sets the threshold\n").formatted(Formatting.WHITE))
                .append(Text.literal("\nEntity Tracking:\n").formatted(Formatting.GOLD))
                .append(Text.literal("/auto-logout entity-tracking enable").formatted(Formatting.GREEN)).append(Text.literal(" - Enables Entity Tracking\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout entity-tracking disable").formatted(Formatting.RED)).append(Text.literal(" - Disables Entity Tracking\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout entity-tracking entity-count").formatted(Formatting.DARK_PURPLE)).append(Text.literal(" - Displays the current Nearby Entity Count\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout entity-tracking entity-count <value>").formatted(Formatting.AQUA)).append(Text.literal(" - Sets the Nearby Entity Count\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout entity-tracking radius").formatted(Formatting.DARK_PURPLE)).append(Text.literal(" - Displays the current Tracking Radius\n").formatted(Formatting.WHITE))
                .append(Text.literal("/auto-logout entity-tracking radius <value>").formatted(Formatting.AQUA)).append(Text.literal(" - Sets the Tracking Radius\n").formatted(Formatting.WHITE))
        );

        return Command.SINGLE_SUCCESS;
    }
}
