package pro.fazeclan.river.stupid_express.mixin.modifier.lovers;

import dev.doctor4t.wathe.cca.GameRoundEndComponent;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.SEModifiers;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

import java.util.ArrayList;

@Mixin(MurderGameMode.class)
public class LoversWinConditionMixin {

    @Inject(
            method = "tickServerGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/wathe/cca/GameWorldComponent;getGameStatus()Ldev/doctor4t/wathe/cca/GameWorldComponent$GameStatus;",
                    opcode = Opcodes.GETSTATIC
            ),
            cancellable = true
    )
    private void loversWinCheck(
            ServerLevel serverWorld, GameWorldComponent gameWorldComponent, CallbackInfo ci
    ) {

        for (ServerPlayer player : serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival)) {
            var component = LoversComponent.KEY.get(player);
            if (component.won()) {
                var ce = CustomWinnerComponent.KEY.get(serverWorld);
                var lovers = new ArrayList<Player>();
                lovers.add(serverWorld.getPlayerByUUID(component.getLover()));
                lovers.add(player);
                ce.setWinningTextId(SEModifiers.LOVERS.identifier().getPath());
                ce.setWinners(lovers);
                ce.setColor(SEModifiers.LOVERS.color());
                ce.sync();

                GameRoundEndComponent.KEY.get(serverWorld)
                        .setRoundEndData(serverWorld.players(), GameFunctions.WinStatus.KILLERS);

                GameFunctions.stopGame(serverWorld);
                ci.cancel();
            }
        }

    }

}
