package de.myownbrain.autoLogout.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoLogoutClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Auto Logout Client Mod Initialized!");
        ConfigManager.loadConfig();
        monitorPlayerHealth();
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
        });
    }

    private void monitorPlayerHealth() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && ConfigManager.isModEnabled) {
                float health = client.player.getHealth();
                if (health <= ConfigManager.healthThreshold) {
                    client.player.networkHandler.getConnection().disconnect(Text.of("You were disconnected due to low health by Auto Logout"));
                }
            }
        });
    }
}
