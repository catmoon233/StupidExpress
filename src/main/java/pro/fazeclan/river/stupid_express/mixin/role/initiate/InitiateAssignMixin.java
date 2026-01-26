package pro.fazeclan.river.stupid_express.mixin.role.initiate;

import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.util.List;

@Mixin(ModdedMurderGameMode.class)
public class InitiateAssignMixin {

    private static void clearModItems(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var item = player.getInventory().getItem(i);
            // 只清除模组物品：刀、汽油桶、打火机，保留钥匙和信件
            if (item.is(TMMItems.KNIFE) ||
                item.is(SEItems.JERRY_CAN) ||
                item.is(SEItems.LIGHTER)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    @Inject(
            method = "assignCivilianReplacingRoles",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/ArrayList;removeIf(Ljava/util/function/Predicate;)Z",
                    ordinal = 4
            )
    )
    private void assignSecondInitiate(
            int desiredRoleCount,
            ServerLevel serverWorld,
            GameWorldComponent gameWorldComponent,
            List<ServerPlayer> players,
            CallbackInfo ci
    ) {
        boolean hasInitiate = players.stream().anyMatch(p -> gameWorldComponent.isRole(p, SERoles.INITIATE));

        if (!hasInitiate) {
            return;
        }

        for (ServerPlayer player : players) {
            if (!gameWorldComponent.isInnocent(player)
                    && !gameWorldComponent.canUseKillerFeatures(player)
                    && !gameWorldComponent.isRole(player, SERoles.INITIATE)) {
                // 只清除模组物品，保留钥匙和信件              
                gameWorldComponent.addRole(player, SERoles.INITIATE);
                clearModItems(player);
                Log.info(LogCategory.GENERAL, player.getScoreboardName() + " -> " + SERoles.INITIATE.identifier().toString());
                break;
            }
        }
    }

}
