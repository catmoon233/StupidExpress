package pro.fazeclan.river.stupid_express.mixin.role.neutral;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.role.neutral.NeutralRoleWorldComponent;

import java.util.UUID;

@Mixin(GameRoundEndComponent.class)
public class NeutralRoundEndMixin {

    @Shadow
    @Final
    private Level world;

    @Inject(
            method = "didWin",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true
    )
    private void didWin(UUID uuid, CallbackInfoReturnable<Boolean> cir, @Local(name = "detail") GameRoundEndComponent.RoundEndData detail) {
        NeutralRoleWorldComponent component = NeutralRoleWorldComponent.KEY.get(world);
        if (!component.hasNeutralWinner()) {
            return;
        }
        var winningRole = component.getWinningText();
        if (detail.role().equals(winningRole)) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

}
