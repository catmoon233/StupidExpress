package pro.fazeclan.river.stupid_express.mixin.role.avaricious;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/doctor4t/trainmurdermystery/cca/PlayerShopComponent;addToBalance(I)V"
            )
    )
    private void injectAvariciousMoney(
            ServerLevel serverWorld,
            GameWorldComponent gameWorldComponent,
            CallbackInfo ci,
            @Local(name = "player") ServerPlayer player
    )
    {
        if (gameWorldComponent.isRole(player, SERoles.AVARICIOUS)) {

            GameTimeComponent timeComponent = GameTimeComponent.KEY.get(serverWorld);
            long currentTime = timeComponent.time;

            int remainder = (int) (currentTime % AvariciousGoldHandler.TIMER_TICKS);
            int ticksRemaining = AvariciousGoldHandler.TIMER_TICKS - remainder;

            boolean isPayoutTime = remainder == 0;

            if ((currentTime % 20) == 0) {
                String actionbarText = "§6§l" + (ticksRemaining / 20);
                player.connection.send(
                        new ClientboundSetActionBarTextPacket(
                                Component.literal(actionbarText)
                        )
                );
            }

            if (isPayoutTime) {

                // TODO: Might want to make the amount of players contributing to
                //  your payout display in the actionbar as well.

                int nearbyPlayers = 0;

                for (ServerPlayer other : serverWorld.players()) {
                    if (other == player) continue;

                    if (other.distanceTo(player) <= AvariciousGoldHandler.MAX_DISTANCE) {
                        nearbyPlayers++;
                    }
                }

                // TODO: Might want to consider scaling.

                if (nearbyPlayers > 0) {
                    PlayerShopComponent.KEY.get(player).addToBalance(nearbyPlayers * 10);
                    player.playSound(TMMSounds.UI_SHOP_BUY, 2.0f, 0.5f);
                }
            }

        }
    }



}
