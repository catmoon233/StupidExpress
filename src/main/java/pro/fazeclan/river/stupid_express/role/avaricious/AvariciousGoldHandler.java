package pro.fazeclan.river.stupid_express.role.avaricious;

import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import pro.fazeclan.river.stupid_express.SERoles;

public class AvariciousGoldHandler {

    public static int TIMER_TICKS = GameConstants.getInTicks(1,0);

    public static double MAX_DISTANCE = 7;

    public static void onGameStart() {
        ModdedRoleAssigned.EVENT.register(((player, role) -> {

            if (role.equals(SERoles.AVARICIOUS)) {
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);

                shop.reset();

            }

        }));
    }

    @Deprecated
    public static void payout() {
        ModdedRoleAssigned.EVENT.register(((player, role) -> {

            if (role.equals(SERoles.AVARICIOUS)) {

                GameTimeComponent timeComponent = GameTimeComponent.KEY.get(player.level());
                boolean payoutTime = timeComponent.time % TIMER_TICKS == 0;

                PlayerShopComponent shop = PlayerShopComponent.KEY.get(player);

                if (payoutTime) {

                    int playerCount = 0;

                    for (Player playerInWorld : player.level().players()) {

                        if (playerInWorld != player) {

                            if (playerInWorld.distanceTo(player) <= MAX_DISTANCE) {

                                playerCount++;

                            }

                        }

                    }

                    shop.addToBalance(playerCount * 10);

                }

            }
        }));

    }

}
