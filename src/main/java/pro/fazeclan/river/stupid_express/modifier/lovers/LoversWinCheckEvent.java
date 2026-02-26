package pro.fazeclan.river.stupid_express.modifier.lovers;

import java.util.ArrayList;
import java.util.UUID;

import org.agmas.harpymodloader.component.WorldModifierComponent;

import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.event.AllowGameEnd;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.WinStatus;
import net.minecraft.server.level.ServerPlayer;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

public class LoversWinCheckEvent {
    public static void register() {
        AllowGameEnd.EVENT.register((serverWorld, winStatus, isLooseend) -> {
            if (isLooseend)
                return WinStatus.NOT_MODIFY;
            var remainingPlayers = serverWorld.getPlayers(GameFunctions::isPlayerAliveAndSurvival);
            var worldModifierComponent = WorldModifierComponent.KEY.get(serverWorld);
            for (ServerPlayer player : remainingPlayers) {
                if (worldModifierComponent.isModifier(player, SEModifiers.LOVERS)) {
                    var loversComponent = LoversComponent.KEY.get(player);
                    if (!loversComponent.isLover()) {
                        continue;
                    }

                    // check for only lovers win condition
                    if (loversComponent.won()) {
                        ArrayList<UUID> lovers = new ArrayList<>();
                        lovers.add((loversComponent.getLover()));
                        lovers.add(player.getUUID());
                        var gameRoundEndComponent = GameRoundEndComponent.KEY.get(serverWorld);
                        gameRoundEndComponent.CustomWinnerColor = SEModifiers.LOVERS.color();
                        gameRoundEndComponent.CustomWinnerID = SEModifiers.LOVERS.identifier().toLanguageKey();
                        gameRoundEndComponent.CustomWinnerPlayers.clear();
                        gameRoundEndComponent.CustomWinnerPlayers.addAll(lovers);
                        return WinStatus.CUSTOM;
                    }
                }
            }
            return WinStatus.NOT_MODIFY;
        });
    }
}
