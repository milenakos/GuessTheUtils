package com.aembr.guesstheutils.mixin.custom_scoreboard;

import com.aembr.guesstheutils.modules.CustomScoreboard;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {
    @Inject(at = @At("HEAD"), method = "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", cancellable = true)
    private void onExtractScoreboardSidebar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomScoreboard.isRendering()) ci.cancel();
    }
}
