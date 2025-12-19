package pro.fazeclan.river.stupid_express.mixin.role.arsonist;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.DousedPlayerComponent;

@Mixin(GameFunctions.class)
public class ArsonistRemoveDousedMixin {

    @Inject(method = "resetPlayer", at = @At("HEAD"))
    private static void resetPlayer(ServerPlayer player, CallbackInfo ci) {
        var component = DousedPlayerComponent.KEY.get(player);
        component.reset();
        component.sync();
    }

}
