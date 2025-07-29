package com.aembr.guesstheutils.mixin.chat_cooldown_timer;

import com.aembr.guesstheutils.GuessTheUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    private void onSendMessage(String content, CallbackInfo ci) {
        GuessTheUtils.chatCooldown.onMessageSent();
    }
}
