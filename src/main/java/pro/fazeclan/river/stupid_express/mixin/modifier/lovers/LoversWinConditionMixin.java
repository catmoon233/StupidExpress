package pro.fazeclan.river.stupid_express.mixin.modifier.lovers;

import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;
import pro.fazeclan.river.stupid_express.role.neutral.NeutralRoleWorldComponent;

@Mixin(MurderGameMode.class)
public class LoversWinConditionMixin {

    @Inject(
            method = "tickServerGameLoop",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/doctor4t/trainmurdermystery/game/GameFunctions$WinStatus;NONE:Ldev/doctor4t/trainmurdermystery/game/GameFunctions$WinStatus;",
                    ordinal = 3,
                    opcode = Opcodes.GETSTATIC
            ),
            cancellable = true
    )
    private void loversWinCheck(
            ServerLevel serverWorld,
            GameWorldComponent gameWorldComponent,
            CallbackInfo ci
    ) {
        for (ServerPlayer player : serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival)) {
            var component = LoversComponent.KEY.get(player);
            if (component.won()) {
                var nrwc = NeutralRoleWorldComponent.KEY.get(serverWorld);
                nrwc.setWinningRole(StupidExpress.LOVERS);
                nrwc.sync();

                GameRoundEndComponent.KEY.get(serverWorld)
                        .setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.KILLERS);

                GameFunctions.stopGame(serverWorld);
                ci.cancel();
            }
        }

    }

}
