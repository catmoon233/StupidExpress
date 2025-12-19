package pro.fazeclan.river.stupid_express.mixin.role.avaricious;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.SERoles;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;

@Mixin(MurderGameMode.class)
public class AvariciousGoldPayout {

    @Inject(
            method = "tickServerGameLoop",
            at = @At("TAIL")
    )
    private void payout(
            ServerLevel serverWorld,
            GameWorldComponent gameWorldComponent,
            CallbackInfo ci
    ) {
        GameTimeComponent timeComponent = GameTimeComponent.KEY.get(serverWorld);
        long time = timeComponent.time;

        if (AvariciousGoldHandler.gameStartTime == -1) {
            AvariciousGoldHandler.gameStartTime = time;
            return;
        }

        long elapsed = time - AvariciousGoldHandler.gameStartTime;

        if (elapsed % AvariciousGoldHandler.TIMER_TICKS != 0) return;

        for (ServerPlayer player : serverWorld.players()) {
            if (!gameWorldComponent.isRole(player, SERoles.AVARICIOUS)) continue;

            int nearbyPlayers = 0;
            for (ServerPlayer other : serverWorld.players()) {
                if (GameFunctions.isPlayerEliminated(other)) continue;
                if (other == player) continue;
                if (other.distanceTo(player) <= AvariciousGoldHandler.MAX_DISTANCE)
                    nearbyPlayers++;
            }

            if (nearbyPlayers > 0) {
                PlayerShopComponent.KEY.get(player).addToBalance(nearbyPlayers * AvariciousGoldHandler.PAYOUT_PER_PLAYER);
                // TODO: Isn't working currently, fix sound cue.
                player.playSound(TMMSounds.UI_SHOP_BUY, 10.0f, 0.5f);
            }
        }
    }

}
