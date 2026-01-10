package pro.fazeclan.river.stupid_express.mixin.modifier.lovers;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

import java.util.ArrayList;

@Mixin(value = MurderGameMode.class, priority = 1500)
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
            CallbackInfo ci,
            @Local(name = "winStatus") GameFunctions.WinStatus winStatus
    ) {

        var config = StupidExpress.CONFIG;
        var loversAlive = false;
        var remainingPlayers = serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival);
        for (ServerPlayer player : remainingPlayers) {
            var loversComponent = LoversComponent.KEY.get(player);
            if (!loversComponent.isLover()) {
                continue;
            }

            loversAlive = true;

            // check for only lovers win condition
            if (loversComponent.won()) {
                var ce = CustomWinnerComponent.KEY.get(serverWorld);
                var lovers = new ArrayList<Player>();
                lovers.add(serverWorld.getPlayerByUUID(loversComponent.getLover()));
                lovers.add(player);
                ce.setWinningTextId(SEModifiers.LOVERS.identifier().getPath());
                ce.setWinners(lovers);
                ce.setColor(SEModifiers.LOVERS.color());
                ce.sync();

                GameRoundEndComponent.KEY.get(serverWorld)
                        .setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.KILLERS);

                GameFunctions.stopGame(serverWorld);
                ci.cancel();
                return;
            }

            // check for lovers with killers win condition
            if (config.modifiersSection.loversSection.loversWinWithKillers) {
                var lover = loversComponent.getLoverAsPlayer();
                if (lover == null) {
                    continue;
                }
                if (gameWorldComponent.isInnocent(player) && gameWorldComponent.isInnocent(lover)) {
                    continue;
                }
                var remainingNonInnocent = remainingPlayers.stream().filter(hostile -> !gameWorldComponent.isInnocent(hostile)).toList();
                if (remainingPlayers.size() - 1 != remainingNonInnocent.size()) {
                    continue;
                }
                GameRoundEndComponent.KEY.get(serverWorld)
                        .setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.KILLERS);
                GameFunctions.stopGame(serverWorld);
                ci.cancel();
                return;
            }
        }

        // check if lovers can't win with civilians, and keep the game going
        if (loversAlive
                && !config.modifiersSection.loversSection.loversWinWithCivilians
                && (winStatus == GameFunctions.WinStatus.KILLERS || winStatus == GameFunctions.WinStatus.PASSENGERS)) {
            ci.cancel();
        }

    }

}
