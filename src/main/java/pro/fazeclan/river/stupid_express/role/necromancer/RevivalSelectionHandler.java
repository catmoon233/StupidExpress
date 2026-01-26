package pro.fazeclan.river.stupid_express.role.necromancer;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import org.agmas.harpymodloader.Harpymodloader;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent;
import pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent;

public class RevivalSelectionHandler {

    public static void init() {
        UseEntityCallback.EVENT.register(((player, level, interactionHand, entity, entityHitResult) -> {
            if (!(player instanceof ServerPlayer interacting)) {
                return InteractionResult.PASS;
            }
            if (!interacting.gameMode.isSurvival()) {
                return InteractionResult.PASS;
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            if (!gameWorldComponent.isRole(player, SERoles.NECROMANCER)) {
                return InteractionResult.PASS;
            }
            if (!(entity instanceof PlayerBodyEntity body)) {
                return InteractionResult.PASS;
            }

            var serverLevel = (ServerLevel) level;

            // check if the selected body can be revived
            var revived = (ServerPlayer) serverLevel.getPlayerByUUID(body.getPlayerUuid());
            if (revived == null) {
                return InteractionResult.PASS;
            }
            var nc = NecromancerComponent.KEY.get(serverLevel);
            if (nc.getAvailableRevives() < 1) {
                return InteractionResult.PASS;
            }

            // activate cooldown
            AbilityCooldownComponent cooldown = AbilityCooldownComponent.KEY.get(player);
            if (cooldown.hasCooldown()) {
                return InteractionResult.PASS;
            }
            cooldown.setCooldown(3 * 60 * 20);
            nc.decreaseAvailableRevives();
            nc.sync();

<<<<<<< HEAD
            // get random killer role
            var roles = new ArrayList<>(TMMRoles.ROLES.values());
            roles.remove(SERoles.NECROMANCER);
            roles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role)
                    || !role.canUseKiller()
                    || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
            if (roles.isEmpty()) {
                roles.add(TMMRoles.KILLER);
            }
            Collections.shuffle(roles);

=======
>>>>>>> a8aba49fde960fb10cde015e91937251af3bf30f
            // revive player and give them the role
            var selectedRole = TMMRoles.KILLER;

            serverLevel.players().forEach(
                    a->{
                        a.playNotifySound( SoundEvents.TOTEM_USE,revived.getSoundSource(), 1.2f, 1.5f);
                        a.sendSystemMessage(Component.translatable("hud.stupid_express.necromancer.revived_player").append(Harpymodloader.getRoleName(selectedRole)),true);
                    }
            );
            revived.teleportTo(body.getX(), body.getY(), body.getZ());
            revived.setGameMode(GameType.ADVENTURE);
            body.remove(Entity.RemovalReason.DISCARDED); // like it never existed

            gameWorldComponent.addRole(revived, selectedRole);

            PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(revived);
            playerShopComponent.setBalance(200);

            ServerPlayNetworking.send( interacting, new AnnounceWelcomePayload(gameWorldComponent.getRole(interacting).getIdentifier().toString(), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));


            return InteractionResult.CONSUME;
        }));
    }

}
