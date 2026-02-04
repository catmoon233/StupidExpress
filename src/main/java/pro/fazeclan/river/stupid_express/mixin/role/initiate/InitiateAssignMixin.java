package pro.fazeclan.river.stupid_express.mixin.role.initiate;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.utils.RoleUtils;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ModdedMurderGameMode.class)
public class InitiateAssignMixin {

    private static final int THIRTY_SECONDS_TICKS = GameConstants.getInTicks(0, 30);

    private static void clearModItems(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var item = player.getInventory().getItem(i);
            // 清除模组物品：刀、汽油桶、打火机
            if (item.is(TMMItems.KNIFE) ||
                    item.is(SEItems.JERRY_CAN) ||
                    item.is(SEItems.LIGHTER)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }


    static {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            if (server.getPlayerList().getPlayers().isEmpty()) {
                return;
            }

            var playerList = server.getPlayerList().getPlayers();
            var level = playerList.getFirst().level();
            var gameWorldComponent = GameWorldComponent.KEY.get(level);
            var gameTimeComponent = GameTimeComponent.KEY.get(level);

            // 检查是否有初学者
            List<ServerPlayer> initiates = playerList.stream()
                    .filter(p -> gameWorldComponent.isRole(p, SERoles.INITIATE))
                    .collect(Collectors.toList());

            int initiateCount = initiates.size();

            // 如果没有初学者，不做任何修改
            if (initiateCount == 0) {
                return;
            }

            // 如果有2个或更多初学者，不做任何修改
            if (initiateCount >= 2) {
                return;
            }

            // 如果只有1个初学者，每隔30秒检查一次
            if (initiateCount == 1) {
                long gameTime = gameTimeComponent.time;

                // 检查是否是30秒的整倍数时刻
                if (gameTime % THIRTY_SECONDS_TICKS == 0) {
                    ServerPlayer initiate = initiates.get(0);
                    clearModItems(initiate);
                    RoleUtils.changeRole(initiate, SERoles.AMNESIAC);
                }
            }
        });
    }

}
