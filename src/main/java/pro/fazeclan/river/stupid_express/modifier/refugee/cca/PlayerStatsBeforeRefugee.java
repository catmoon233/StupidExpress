package pro.fazeclan.river.stupid_express.modifier.refugee.cca;

import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record PlayerStatsBeforeRefugee(Vec3 pos, int money, ListTag inventory, Vec2 rotation, boolean isAlive,
        float mood) {
    // 期间死亡的其它玩家会复活，玩家物品栏、金币、位置重置到亡命徒复活的时刻
    public static void LoadToPlayer(ServerPlayer player, PlayerStatsBeforeRefugee playerStats) {
        if (playerStats == null)
            return;
        if (!playerStats.isAlive())
            return;
        player.getInventory().load(playerStats.inventory());
        player.setPos(playerStats.pos());
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            player.setGameMode(GameType.ADVENTURE);
        }
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
                shopComponent.balance, listTag, player.getRotationVector(),
                GameFunctions.isPlayerAliveAndSurvival(player), moodComponent.getMood());
        return playerStats;
    }
}
