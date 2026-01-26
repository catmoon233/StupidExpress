package pro.fazeclan.river.stupid_express.role.amnesiac;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import pro.fazeclan.river.stupid_express.constants.SERoles;

public class RoleSelectionHandler {

    private static void clearAllKnives(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(TMMItems.KNIFE)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    public static void init() {
        UseEntityCallback.EVENT.register(((player, level, interactionHand, entity, entityHitResult) -> {
            if (!(player instanceof ServerPlayer interacting)) {
                return InteractionResult.PASS;
            }
            if (!interacting.gameMode.isSurvival()) {
                return InteractionResult.PASS;
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            if (!gameWorldComponent.isRole(player, SERoles.AMNESIAC)) {
                return InteractionResult.PASS;
            }
            if (!(entity instanceof PlayerBodyEntity victim)) {
                return InteractionResult.PASS;
            }
            Role role = gameWorldComponent.getRole(victim.getPlayerUuid());

            // 清除物品栏中的所有刀
            clearAllKnives(interacting);

            PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(interacting);

            gameWorldComponent.addRole(interacting, role);
            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(interacting, role);
            playerShopComponent.setBalance(200);
            ServerPlayNetworking.send(interacting, new AnnounceWelcomePayload(gameWorldComponent.getRole(interacting).getIdentifier().toString(), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));


            return InteractionResult.CONSUME;
        }));
    }

}