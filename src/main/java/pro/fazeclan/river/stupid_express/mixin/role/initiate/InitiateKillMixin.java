package pro.fazeclan.river.stupid_express.mixin.role.initiate;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.utils.RoleUtils;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.ArrayList;
import java.util.Collections;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

@Mixin(GameFunctions.class)
public abstract class InitiateKillMixin {

    private static void clearAllKnives(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(TMMItems.KNIFE)) {
                player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
    }

    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void initiateKill(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason,
            CallbackInfo ci) {
        if (!(victim instanceof ServerPlayer)) {
            return;
        }

        var level = (ServerLevel) victim.level();
        var gameWorldComponent = GameWorldComponent.KEY.get(level);

        if (!gameWorldComponent.isRole(victim, SERoles.INITIATE)) {
            return;
        }
        if (killer != null && gameWorldComponent.isRole(killer, SERoles.INITIATE)) {
            var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
            shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller()
                    || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath())
                    || role.identifier().equals(fromNamespaceAndPath("noellesroles", "poisoner")));
            if (shuffledKillerRoles.isEmpty())
                shuffledKillerRoles.add(TMMRoles.KILLER);
            Collections.shuffle(shuffledKillerRoles);

            var role = shuffledKillerRoles.getFirst();
            RoleUtils.changeRole(killer, role, false);
            TMM.REPLAY_MANAGER.recordPlayerRevival(killer.getUUID(), role);

            // 清除物品栏中的所有刀
            clearAllKnives(killer);

            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(killer, role);
            ServerPlayNetworking.send((ServerPlayer) killer,
                    new AnnounceWelcomePayload(gameWorldComponent.getRole(killer).getIdentifier().toString(),
                            gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

        }
    }

    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"), cancellable = true)
    private static void initiateKillNonInitiate(
            Player victim,
            boolean spawnBody,
            Player killer,
            ResourceLocation deathReason,
            CallbackInfo ci) {
        if (!(victim instanceof ServerPlayer)) {
            return;
        }

        var level = (ServerLevel) victim.level();
        var gameWorldComponent = GameWorldComponent.KEY.get(level);
        if (!gameWorldComponent.isRole(victim, SERoles.INITIATE) && killer != null
                && gameWorldComponent.isRole(killer, SERoles.INITIATE)) {
            Role newInitiateRole;
            switch (StupidExpress.CONFIG.rolesSection.initiateSection.initiateFallbackRole) {
                case KILLER -> {
                    var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                    shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role)
                            || !role.canUseKiller()
                            || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath())
                            || role.identifier().equals(fromNamespaceAndPath("noellesroles", "poisoner")));
                    if (shuffledKillerRoles.isEmpty())
                        shuffledKillerRoles.add(TMMRoles.KILLER);
                    Collections.shuffle(shuffledKillerRoles);

                    newInitiateRole = shuffledKillerRoles.getFirst();
                }
                case NEUTRAL -> {
                    var shuffledNeutralRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                    shuffledNeutralRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role)
                            || role.canUseKiller() || role.isInnocent() || role.equals(SERoles.AMNESIAC)
                            || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
                    if (shuffledNeutralRoles.isEmpty())
                        shuffledNeutralRoles.add(SERoles.AMNESIAC);
                    Collections.shuffle(shuffledNeutralRoles);

                    newInitiateRole = shuffledNeutralRoles.getFirst();
                }
                case null, default -> newInitiateRole = SERoles.AMNESIAC;
            }
            for (ServerPlayer player : level.getPlayers(p -> gameWorldComponent.isRole(p, SERoles.INITIATE))) {
                // 清除物品栏中的所有刀
                clearAllKnives(player);
                RoleUtils.changeRole(player, newInitiateRole);
                // ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, newInitiateRole);

                // TMM.REPLAY_MANAGER.recordPlayerRoleChange(player.getUUID(), SERoles.INITIATE, newInitiateRole);

                ServerPlayNetworking.send(player,
                        new AnnounceWelcomePayload(gameWorldComponent.getRole(player).getIdentifier().toString(),
                                gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

            }
            if (!spawnBody) {
                victim.teleportTo(killer.getX(), killer.getY(), killer.getZ());
                victim.fallDistance = 0.0f;
            }
            GameFunctions.killPlayer(killer, true, null, StupidExpress.id("failed_initiation"));
            ci.cancel();
        } else if (gameWorldComponent.isRole(victim, SERoles.INITIATE) && killer != null
                && !gameWorldComponent.isRole(killer, SERoles.INITIATE)) {
            Role newInitiateRole;
            switch (StupidExpress.CONFIG.rolesSection.initiateSection.initiateFallbackRole) {
                case KILLER -> {
                    var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                    shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role)
                            || !role.canUseKiller()
                            || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath())
                            || role.identifier().equals(fromNamespaceAndPath("noellesroles", "poisoner")));
                    if (shuffledKillerRoles.isEmpty())
                        shuffledKillerRoles.add(TMMRoles.KILLER);
                    Collections.shuffle(shuffledKillerRoles);

                    newInitiateRole = shuffledKillerRoles.getFirst();
                }
                case NEUTRAL -> {
                    var shuffledNeutralRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                    shuffledNeutralRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role)
                            || role.canUseKiller() || role.isInnocent() || role.equals(SERoles.AMNESIAC)
                            || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
                    if (shuffledNeutralRoles.isEmpty())
                        shuffledNeutralRoles.add(SERoles.AMNESIAC);
                    Collections.shuffle(shuffledNeutralRoles);

                    newInitiateRole = shuffledNeutralRoles.getFirst();
                }
                case null, default -> newInitiateRole = SERoles.AMNESIAC;
            }
            for (ServerPlayer player : level.getPlayers(p -> gameWorldComponent.isRole(p, SERoles.INITIATE))) {
                // 清除物品栏中的所有刀
                clearAllKnives(player);

                RoleUtils.changeRole(player, newInitiateRole);

                // ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, newInitiateRole);

                // TMM.REPLAY_MANAGER.recordPlayerRoleChange(player.getUUID(), SERoles.INITIATE, newInitiateRole);

                ServerPlayNetworking.send(player,
                        new AnnounceWelcomePayload(gameWorldComponent.getRole(player).getIdentifier().toString(),
                                gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

            }
        }
    }

}
