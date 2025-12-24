package pro.fazeclan.river.stupid_express.mixin.role;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;

import java.util.UUID;

@Mixin(GameRoundEndComponent.class)
public class CustomWinnerCheckMixin {

    @Shadow
    @Final
    private Level world;

    @Inject(
            method = "didWin",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true
    )
    private void didWin(UUID uuid, CallbackInfoReturnable<Boolean> cir, @Local(name = "detail") GameRoundEndComponent.RoundEndData detail) {
        var component = CustomWinnerComponent.KEY.get(world);
        if (!component.hasCustomWinner()) {
            return;
        }
        var assumedWinning = component.getWinningTextId();
        var winningTextId = detail.role().roleText.getString();
        if (winningTextId.equals(assumedWinning)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

}
