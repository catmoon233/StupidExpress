package pro.fazeclan.river.stupid_express.role.necromancer;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
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
import org.jetbrains.annotations.NotNull;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent;
import pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent;
import pro.fazeclan.river.stupid_express.utils.RoleUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin.*;

public class RevivalSelectionHandler {
    public static void removeVoice(@NotNull UUID player) {
        if (SERVER_API == null) {
            return;
        }
        VoicechatConnection connection = SERVER_API.getConnectionOf(player);
        if (connection != null) {
            connection.setGroup(null);
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

            // get random killer role
            var roles = new ArrayList<Role>();
            roles.add(TMMRoles.KILLER);
            Collections.shuffle(roles);

            // revive player and give them the role
            var selectedRole = roles.getFirst();

            serverLevel.players().forEach(
                    a -> {
                        a.playNotifySound(SoundEvents.TOTEM_USE, revived.getSoundSource(), 1.2f, 1.5f);
                        a.sendSystemMessage(Component.translatable("hud.stupid_express.necromancer.revived_player")
                                .append(Harpymodloader.getRoleName(selectedRole)), true);
                    });
            revived.getInventory().clearContent();
            revived.teleportTo(body.getX(), body.getY(), body.getZ());
            revived.setGameMode(GameType.ADVENTURE);
            removeVoice(revived.getUUID());
            body.remove(Entity.RemovalReason.DISCARDED); // like it never existed

            RoleUtils.changeRole(revived, selectedRole);
            TMM.REPLAY_MANAGER.recordPlayerRevival(revived.getUUID(), selectedRole);
            PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(revived);
            playerShopComponent.setBalance(200);

            if (Harpymodloader.VANNILA_ROLES.contains(selectedRole)) {
                ServerPlayNetworking.send(
                        revived,
                        new AnnounceWelcomePayload(
                                TMMRoles.KILLER.identifier().getPath(),
                                gameWorldComponent.getAllKillerTeamPlayers().size(),
                                0));
            } else {
                ServerPlayNetworking.send(
                        revived,
                        new AnnounceWelcomePayload(
                                selectedRole.identifier().getPath(),
                                gameWorldComponent.getAllKillerTeamPlayers().size(),
                                0));
            }

            return InteractionResult.CONSUME;
        }));
    }

}
