package de.myownbrain.autoLogout.client;

import com.mojang.brigadier.Command;
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
                                    .executes(ctx -> showCurrentThreshold(ctx.getSource()))
                                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 20))
                                            .executes(ctx -> setThreshold(IntegerArgumentType.getInteger(ctx, "value")))
                                    )
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

    private static int showCurrentThreshold(FabricClientCommandSource source) {
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

    private static int showHelp(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("Auto Logout Commands:\n")
                .styled(style -> style.withBold(true).withColor(Formatting.GOLD))
                .append(Text.literal("/auto-logout enable").styled(style -> style.withColor(Formatting.GREEN)).append(Text.literal(" - Enables the mod\n").styled(style -> style.withColor(Formatting.WHITE).withBold(false))))
                .append(Text.literal("/auto-logout disable").styled(style -> style.withColor(Formatting.RED)).append(Text.literal(" - Disables the mod\n").styled(style -> style.withColor(Formatting.WHITE).withBold(false))))
                .append(Text.literal("/auto-logout threshold <value>").styled(style -> style.withColor(Formatting.AQUA)).append(Text.literal(" - Sets the threshold\n").styled(style -> style.withColor(Formatting.WHITE).withBold(false))))
                .append(Text.literal("/auto-logout threshold").styled(style -> style.withColor(Formatting.DARK_PURPLE)).append(Text.literal(" - Displays the current threshold").styled(style -> style.withColor(Formatting.WHITE).withBold(false)))))
        ;
        return Command.SINGLE_SUCCESS;
    }
}
