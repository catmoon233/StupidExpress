package pro.fazeclan.river.stupid_express.client.mixin.modifier.allergic;

import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.modifier.allergic.cca.AllergicComponent;

import java.awt.*;

@Mixin(WatheClient.class)
public abstract class InstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void isInstinctEnabled(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().player != null) {
            AllergicComponent allergy = AllergicComponent.KEY.get(Minecraft.getInstance().player);
            if (allergy.isAllergic() && allergy.getGlowTicks() > 0) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        if (target instanceof Player) {
            if (!target.isSpectator()) {
                if (Minecraft.getInstance().player != null) {
                    AllergicComponent allergy = AllergicComponent.KEY.get(Minecraft.getInstance().player);
                    if (allergy.isAllergic() && allergy.getGlowTicks() > 0) {
                        cir.setReturnValue(Color.GREEN.getRGB());
                    }
                }
            }
        }
    }
}
