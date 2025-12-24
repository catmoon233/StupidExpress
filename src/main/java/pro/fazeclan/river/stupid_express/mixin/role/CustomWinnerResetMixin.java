package pro.fazeclan.river.stupid_express.mixin.role;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;

@Mixin(GameFunctions.class)
public class CustomWinnerResetMixin {

    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void initializeGame(ServerLevel serverWorld, CallbackInfo ci) {
        CustomWinnerComponent component = CustomWinnerComponent.KEY.get(serverWorld);
        component.reset();
    }

}
