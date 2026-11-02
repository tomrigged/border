package dev.tomax.renderborder.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side only: every tick, the (client copy of the) world border is centered on the player
 * with its edge just inside the render distance. The vanilla renderer then draws the normal
 * world border texture there. The server's real border is not changed.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void renderborder$tick(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.level == null || mc.player == null) {
            return;
        }
        WorldBorder border = mc.level.getWorldBorder();
        double half = Math.max(16.0, mc.options.getEffectiveRenderDistance() * 16.0 - 4.0);
        border.setCenter(mc.player.getX(), mc.player.getZ());
        border.setSize(half * 2.0);
        border.setWarningBlocks(0);
        border.setWarningTime(0);
    }
}
