package io.github.aws404.mixin;

import io.github.aws404.util.gui.CustomHandler;
import io.github.aws404.util.input.ChatInput;
import io.github.aws404.util.input.SignInput;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
    private void onClickSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (player.currentScreenHandler instanceof CustomHandler) {
            player.currentScreenHandler.onSlotClick(packet.getSlot(), packet.getClickData(), packet.getActionType(), player);
            player.onHandlerRegistered(player.currentScreenHandler, player.currentScreenHandler.getStacks());
            ci.cancel();
        }
    }

    @Inject(method = "onSignUpdate", at = @At("HEAD"), cancellable = true)
    private void onSignUpdate(UpdateSignC2SPacket packet, CallbackInfo ci) {
        if (SignInput.HANDLERS.containsKey(player)) {
            SignInput.HANDLERS.get(player).onInput(packet.getPos(), packet.getText());
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void onGameMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (ChatInput.HANDLERS.containsKey(player)) {
            ChatInput.HANDLERS.get(player).onInput(packet.getChatMessage());
            ci.cancel();
        }
    }
}
