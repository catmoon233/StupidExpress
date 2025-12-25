package pro.fazeclan.river.stupid_express.mixin.role.arsonist;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.List;

@Mixin(MurderGameMode.class)
public class ArsonistKeepAliveMixin {

    @Inject(
            method = "tickServerGameLoop",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;NONE:Ldev/doctor4t/wathe/game/GameFunctions$WinStatus;",
                    ordinal = 3,
                    opcode = Opcodes.GETSTATIC
            ),
            cancellable = true
    )
    private void keepAlive(
            ServerLevel serverWorld, GameWorldComponent gameWorldComponent, CallbackInfo ci, @Local(name = "winStatus") GameFunctions.WinStatus winStatus
    ) {
        var config = StupidExpress.CONFIG;
        if (config.rolesSection.arsonistSection.arsonistKeepsGameGoing) {
            var players = serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival);
            boolean arsonistAlive = false;
            for (ServerPlayer player : players) {
                if (gameWorldComponent.isRole(player, SERoles.ARSONIST)) {
                    arsonistAlive = true;
                }
            }

            if (players.size() == 1 && arsonistAlive) {
                var nrwc = CustomWinnerComponent.KEY.get(serverWorld);
                nrwc.setWinningTextId(SERoles.ARSONIST.identifier().getPath());
                nrwc.setWinners(List.of(players.getFirst()));
                nrwc.setColor(SERoles.ARSONIST.color());
                nrwc.sync();
                GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.KILLERS);

                GameFunctions.stopGame(serverWorld);
            }

            if (arsonistAlive && (winStatus == GameFunctions.WinStatus.KILLERS || winStatus == GameFunctions.WinStatus.PASSENGERS)) {
                ci.cancel();
            }
        }
    }

}
