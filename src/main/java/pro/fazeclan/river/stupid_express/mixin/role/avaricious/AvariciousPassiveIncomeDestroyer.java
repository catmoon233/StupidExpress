package pro.fazeclan.river.stupid_express.mixin.role.avaricious;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import pro.fazeclan.river.stupid_express.SERoles;

@Mixin(MurderGameMode.class)
public class AvariciousPassiveIncomeDestroyer {

    @ModifyExpressionValue(
            method = "tickServerGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private boolean noPassiveIncomeKiller(
            boolean original,
            @Local(name = "gameWorldComponent") GameWorldComponent component,
            @Local(name = "player") ServerPlayer player
    ) {
        return original && !component.isRole(player, SERoles.AVARICIOUS);
    }
}