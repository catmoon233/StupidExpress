package pro.fazeclan.river.stupid_express.mixin.role.avaricious;

import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;

@Mixin(MurderGameMode.class)
public class AvariciousGoldPayout {

    @Inject(method = "tickServerGameLoop", at = @At("TAIL"))
    private void payout(
            ServerLevel serverWorld, GameWorldComponent gameWorldComponent, CallbackInfo ci) {
        GameTimeComponent timeComponent = GameTimeComponent.KEY.get(serverWorld);
        long time = timeComponent.time;

        if (AvariciousGoldHandler.gameStartTime == -1) {
            AvariciousGoldHandler.gameStartTime = time;
            return;
        }

        long elapsed = time - AvariciousGoldHandler.gameStartTime;
        long timeinterval = elapsed % (long) AvariciousGoldHandler.TIMER_TICKS;
        if (timeinterval < 0)
            timeinterval = -timeinterval;

        if (elapsed % AvariciousGoldHandler.TIMER_TICKS != 0) {
            if (elapsed % 20 == 0) {
                for (ServerPlayer player : serverWorld.players()) {
                    if (!gameWorldComponent.isRole(player, SERoles.AVARICIOUS))
                        continue;
                    player.sendSystemMessage(
                            Component.translatable(
                                    "hud.stupid_express.avaricious.payout_timer",
                                    ((AvariciousGoldHandler.TIMER_TICKS - timeinterval) / 20))
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                            true);
                }
            }
            return;
        }

        for (ServerPlayer player : serverWorld.players()) {
            if (!gameWorldComponent.isRole(player, SERoles.AVARICIOUS))
                continue;

            int nearbyPlayers = 0;
            for (ServerPlayer other : serverWorld.players()) {
                if (GameFunctions.isPlayerEliminated(other))
                    continue;
                if (other == player)
                    continue;
                if (other.distanceTo(player) <= AvariciousGoldHandler.MAX_DISTANCE)
                    nearbyPlayers++;
            }

            if (nearbyPlayers > 0) {
                int totalPlayers = serverWorld.players().size();
                // 计算平均距离
                double totalDistance = 0.0;
                for (ServerPlayer other : serverWorld.players()) {
                    if (GameFunctions.isPlayerEliminated(other))
                        continue;
                    if (other == player)
                        continue;
                    if (other.distanceTo(player) <= AvariciousGoldHandler.MAX_DISTANCE) {
                        totalDistance += other.distanceTo(player);
                    }
                }
                double avgDistance = totalDistance / nearbyPlayers;

                int payoutPerPlayer = AvariciousGoldHandler.calculatePayout(totalPlayers, nearbyPlayers, avgDistance);
                int totalPayout = nearbyPlayers * payoutPerPlayer;
                // 确保不超过150金币上限
                totalPayout = Math.min(totalPayout, 150);
                PlayerShopComponent.KEY.get(player).addToBalance(totalPayout);
                player.playNotifySound(TMMSounds.UI_SHOP_BUY, SoundSource.PLAYERS, 10.0f, 0.5f);
            }
        }
    }

}
