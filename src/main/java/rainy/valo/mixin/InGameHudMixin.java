package rainy.valo.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rainy.valo.Client.ClientHudState;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void valo$hideHearts(DrawContext context, PlayerEntity player, int x, int y, int lines,
                                 int regeneratingHeartIndex, float maxHealth, int lastHealth, int health,
                                 int absorption, boolean blinking, CallbackInfo ci) {
        if (ClientHudState.data != null) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, require = 0)
    private void valo$hideFood(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        if (ClientHudState.data != null) ci.cancel();
    }
}