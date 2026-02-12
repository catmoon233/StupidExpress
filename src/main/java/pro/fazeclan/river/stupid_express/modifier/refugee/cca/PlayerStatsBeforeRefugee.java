package pro.fazeclan.river.stupid_express.modifier.refugee.cca;

import org.agmas.harpymodloader.component.WorldModifierComponent;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
import dev.doctor4t.trainmurdermystery.event.OnPlayerDeath;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;

public record PlayerStatsBeforeRefugee(Vec3 pos, int money, ListTag inventory, Vec2 rotation, boolean isAlive,
        float mood) {
    // 期间死亡的其它玩家会复活，玩家物品栏、金币、位置重置到亡命徒复活的时刻
    public static void RegisterDeathEvent() {
        (OnPlayerDeath.EVENT).register((victim, deathReason) -> {
            var level = victim.level();
            var worldModifierComponent = WorldModifierComponent.KEY.get(level);

            if (worldModifierComponent.isModifier(victim.getUUID(), SEModifiers.REFUGEE)) {
                var refugeeComponent = RefugeeComponent.KEY.get(level);
                Vec3 pos = GameFunctions.getSpawnPos(AreasWorldComponent.KEY.get(level),
                        GameFunctions.roomToPlayer.get(victim.getUUID()));
                if (pos != null) {
                    refugeeComponent.addPendingRevival(victim.getUUID(), pos.x(), pos.y() + 1, pos.z());
                } else {
                    refugeeComponent.addPendingRevival(victim.getUUID(), victim.getX(), victim.getY(), victim.getZ());
                }
            }
        });
    }

    public static void LoadToPlayer(ServerPlayer player, PlayerStatsBeforeRefugee playerStats, Role role) {
        if (playerStats == null)
            return;
        if (!playerStats.isAlive())
            return;
        player.getInventory().clearContent();
        player.getInventory().load(playerStats.inventory());

        player.teleportTo(playerStats.pos().x, playerStats.pos().y, playerStats.pos().z);
        player.setXRot(playerStats.rotation().x);
        player.setYRot(playerStats.rotation().y);
        player.setPos(playerStats.pos());
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            TMM.REPLAY_MANAGER.recordPlayerRevival(player.getUUID(), role);
            player.setGameMode(GameType.ADVENTURE);
        }
        TrainVoicePlugin.resetPlayer(player.getUUID());

        var shopComponent = PlayerShopComponent.KEY.get(player);
        var moodComponent = PlayerMoodComponent.KEY.get(player);
        shopComponent.balance = playerStats.money();
        moodComponent.setMood(playerStats.mood());
        shopComponent.sync();
        moodComponent.sync();
    }

    public static PlayerStatsBeforeRefugee SaveFromPlayer(ServerPlayer player) {
        var inventory = player.getInventory();
        ListTag listTag = new ListTag();
        inventory.save(listTag);

        var shopComponent = PlayerShopComponent.KEY.get(player);
        var moodComponent = PlayerMoodComponent.KEY.get(player);
        var playerStats = new PlayerStatsBeforeRefugee(player.position(),
                shopComponent.balance, listTag.copy(), player.getRotationVector(),
                GameFunctions.isPlayerAliveAndSurvival(player), moodComponent.getMood());
        return playerStats;
    }
}
