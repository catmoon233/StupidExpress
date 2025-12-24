package pro.fazeclan.river.stupid_express.mixin.role.arsonist;

import com.llamalad7.mixinextras.sugar.Local;
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
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.cca.SEConfig;

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
        var config = SEConfig.KEY.get(serverWorld);
        if (config.isArsonistKeepsGameGoing()) {
            boolean arsonistAlive = false;
            for (ServerPlayer player : serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival)) {
                if (gameWorldComponent.isRole(player, SERoles.ARSONIST)) {
                    arsonistAlive = true;
                }
            }

            if (arsonistAlive && (winStatus == GameFunctions.WinStatus.KILLERS || winStatus == GameFunctions.WinStatus.PASSENGERS)) {
                ci.cancel();
            }
        }
    }

}
