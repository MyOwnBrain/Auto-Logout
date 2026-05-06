package de.myownbrain.autoLogout.client.mixin;

import de.myownbrain.autoLogout.client.TPSMonitor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class TPSTrackerMixin {
    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void onHandleTimeUpdate(ClientboundSetTimePacket packet, CallbackInfo ci) {
        TPSMonitor.onTimePacketReceived(packet.gameTime());
    }

    @Inject(method = "clearLevel", at = @At("RETURN"))
    private void onHandleLevelChange(CallbackInfo ci) {
        TPSMonitor.reset();
    }
}
