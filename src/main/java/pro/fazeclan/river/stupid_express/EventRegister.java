package pro.fazeclan.river.stupid_express;

import java.util.ArrayList;
import java.util.Collections;

import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.OnPlayerDeathWithKiller;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.modifier.cursed.cca.CursedComponent;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent;
import pro.fazeclan.river.stupid_express.utils.StupidRoleUtils;

public class EventRegister {
    private static void clearAllKnives(Player player) {
        TMMItemUtils.clearItem(player, TMMItems.KNIFE);
    }

    public static void register() {
        OnPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {
            var component = GameWorldComponent.KEY.get(victim.level());
            if (component.canUseKillerFeatures(victim)) {
                var nc = NecromancerComponent.KEY.get(victim.level());
                nc.increaseAvailableRevives();
                nc.sync();
            }
        });
        OnPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {
            var level = (ServerLevel) victim.level();
            var gameWorldComponent = GameWorldComponent.KEY.get(level);

            if (!gameWorldComponent.isRole(victim, SERoles.INITIATE)) {
                return;
            }
            if (killer != null && gameWorldComponent.isRole(killer, SERoles.INITIATE)) {
                var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                shuffledKillerRoles.removeIf(role -> Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller()
                        || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath())
                        || role.identifier().equals(ResourceLocation.fromNamespaceAndPath("noellesroles", "poisoner")));
                if (shuffledKillerRoles.isEmpty())
                    shuffledKillerRoles.add(TMMRoles.KILLER);
                Collections.shuffle(shuffledKillerRoles);

                var role = shuffledKillerRoles.getFirst();
                StupidRoleUtils.changeRole(killer, role, false);
                TMM.REPLAY_MANAGER.recordPlayerRevival(killer.getUUID(), role);

                // 清除物品栏中的所有刀
                clearAllKnives(killer);

                ModdedRoleAssigned.EVENT.invoker().assignModdedRole(killer, role);
                ServerPlayNetworking.send((ServerPlayer) killer,
                        new AnnounceWelcomePayload(gameWorldComponent.getRole(killer).getIdentifier().toString(),
                                gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

            }
        });
        OnPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {

            var level = (ServerLevel) victim.level();
            var gameWorldComponent = GameWorldComponent.KEY.get(level);
            if (!gameWorldComponent.isRole(victim, SERoles.INITIATE) && killer != null
                    && gameWorldComponent.isRole(killer, SERoles.INITIATE)) {
                Role newInitiateRole;
                newInitiateRole = SERoles.AMNESIAC;

                for (ServerPlayer player : level.getPlayers(p -> gameWorldComponent.isRole(p, SERoles.INITIATE))) {
                    // 清除物品栏中的所有刀
                    clearAllKnives(player);
                    StupidRoleUtils.changeRole(player, newInitiateRole);

                    ServerPlayNetworking.send(player,
                            new AnnounceWelcomePayload(gameWorldComponent.getRole(player).getIdentifier().toString(),
                                    gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
                }
                GameFunctions.killPlayer(killer, true, null, StupidExpress.id("failed_initiation"));
                return;
            } else if (gameWorldComponent.isRole(victim, SERoles.INITIATE)
                    && (killer == null || !gameWorldComponent.isRole(killer, SERoles.INITIATE))) {
                // 初学者被杀死（包括被炸弹炸死、摔死等非玩家攻击，以及被非初学者玩家杀死）
                Role newInitiateRole;
                if (killer == null) {
                    newInitiateRole = SERoles.AMNESIAC;
                } else {
                    Role killerRole = gameWorldComponent.getRole(killer);
                    if (killerRole == null) {
                        newInitiateRole = SERoles.INITIATE;
                    } else if (gameWorldComponent.isKillerTeamRole(killerRole)) {
                        // 狼杀死
                        var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                        shuffledKillerRoles.removeIf(role -> {
                            if (gameWorldComponent.isKillerTeamRole(role))
                                return true;
                            if (role.isNeutrals())
                                return true;
                            return false;
                        });
                        if (shuffledKillerRoles.isEmpty())
                            shuffledKillerRoles.add(TMMRoles.CIVILIAN);
                        Collections.shuffle(shuffledKillerRoles);

                        newInitiateRole = shuffledKillerRoles.getFirst();
                    } else if (killerRole.isNeutrals()) {
                        // 中立杀死
                        var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                        shuffledKillerRoles.removeIf(role -> {
                            if (role.isNeutralForKiller())
                                return true;
                            if (role.isNeutrals())
                                return false;
                            return true;
                        });
                        if (shuffledKillerRoles.isEmpty())
                            shuffledKillerRoles.add(SERoles.AMNESIAC);
                        Collections.shuffle(shuffledKillerRoles);
                        newInitiateRole = shuffledKillerRoles.getFirst();
                    } else {
                        // 好人杀死
                        var shuffledKillerRoles = new ArrayList<>(StupidExpress.getEnableRoles());
                        shuffledKillerRoles.removeIf(role -> {
                            if (gameWorldComponent.isKillerTeamRole(role))
                                return false;
                            return true;
                        });
                        if (shuffledKillerRoles.isEmpty())
                            shuffledKillerRoles.add(TMMRoles.KILLER);
                        Collections.shuffle(shuffledKillerRoles);

                        newInitiateRole = shuffledKillerRoles.getFirst();
                    }
                }

                for (ServerPlayer player : level.getPlayers(p -> gameWorldComponent.isRole(p, SERoles.INITIATE))) {
                    // 清除物品栏中的所有刀
                    clearAllKnives(player);

                    StupidRoleUtils.changeRole(player, newInitiateRole);

                    // ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, newInitiateRole);

                    // TMM.REPLAY_MANAGER.recordPlayerRoleChange(player.getUUID(), SERoles.INITIATE,
                    // newInitiateRole);

                    ServerPlayNetworking.send(player,
                            new AnnounceWelcomePayload(gameWorldComponent.getRole(player).getIdentifier().toString(),
                                    gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

                }
            }
        });
        OnPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {
            LoversComponent component = LoversComponent.KEY.get(victim);

            if (!component.isLover()) {
                return;
            }

            var level = victim.level();
            var lover = level.getPlayerByUUID(component.getLover());
            if (lover != null) {
                if (GameFunctions.isPlayerAliveAndSurvival(lover)) {
                    GameFunctions.killPlayer(
                            lover,
                            true,
                            killer,
                            StupidExpress.id("broken_heart"));
                } else {
                    var wmc = WorldModifierComponent.KEY.get(lover.level());
                    if (wmc.isModifier(lover, SEModifiers.SPLIT_PERSONALITY)) {
                        var splc = SplitPersonalityComponent.KEY.get(lover);
                        if (!splc.isDeath()) {
                            GameFunctions.killPlayer(
                                    lover,
                                    true,
                                    killer,
                                    StupidExpress.id("broken_heart"));
                        }
                    }

                }

            }
        });
        OnPlayerDeathWithKiller.EVENT.register((victim, killer, deathReason) -> {
            CursedComponent cursedComponent = CursedComponent.KEY.get(victim);

            if (cursedComponent.isCursed() && killer != null) {
                // Transfer curse
                cursedComponent.reset();
                WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(victim.level());
                worldModifierComponent.removeModifier(victim.getUUID(), SEModifiers.CURSED);

                CursedComponent killerCursedComponent = CursedComponent.KEY.get(killer);
                killerCursedComponent.setCursed(killer.getUUID());
                killerCursedComponent.sync();
                worldModifierComponent.addModifier(killer.getUUID(), SEModifiers.CURSED);
            }
        });
    }
}
