package pro.fazeclan.river.stupid_express.mixin.modifier.loose_end;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.network.RemoveStatusBarPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.ArsonistDousedCountComponent;

@Mixin(GameFunctions.class)
public class LooseEndBarClearMixin {
    @Inject(method = "stopGame", at = @At("HEAD"))
    private static void stopGame(ServerLevel world, CallbackInfo ci){
        world.players().forEach(serverPlayer -> {
            RemoveStatusBarPayload payload = new RemoveStatusBarPayload("loose_end");
            ServerPlayNetworking.send(serverPlayer, payload);
        });

        // 重置纵火犯泼油计数器
        var dousedCountComponent = ArsonistDousedCountComponent.KEY.get(world);
        dousedCountComponent.resetDousedCount();
    }
}
