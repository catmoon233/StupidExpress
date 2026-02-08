package pro.fazeclan.river.stupid_express.modifier.split_personality;

import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.network.SplitBackCamera;

import java.util.*;

public class SplitPersonalityHandler {

    // 存储每对双重人格的库存和ender箱
    private static final Map<UUID, ItemStack[]> personalityInventories = new HashMap<>();
    private static final Map<UUID, ItemStack[]> personalityEnderChests = new HashMap<>();

    // 监听双重人格的替换者
    private static final Set<UUID> switchingWatchers = new HashSet<>();

    public static void init() {
        // 注册死亡事件 - 处理双重人格死亡时的倒计时选择
        AllowPlayerDeath.EVENT.register((victim, deathReason) -> {
            if (!(victim instanceof ServerPlayer serverVictim))
                return true;

            var component = SplitPersonalityComponent.KEY.get(serverVictim);

            // 检查是否是双重人格
            if (component.getMainPersonality() == null || component.getSecondPersonality() == null) {
                return true;
            }
            if (component.getTemporaryRevivalStartTick() > 0) {
                ServerPlayNetworking.send(serverVictim, new SplitBackCamera());
                WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(serverVictim.level());
                modifierComponent.removeModifier(serverVictim.getUUID(), SEModifiers.SPLIT_PERSONALITY);
                component.reset();
                return true;
            }
            component.setDeath(true);
            // 直接处理死亡选择逻辑
            handleDeathChoices(serverVictim, component);

            // 保存当前库存
            ItemStack[] inventory = new ItemStack[36]; // 标准库存大小
            for (int i = 0; i < 36; i++) {
                inventory[i] = serverVictim.getInventory().getItem(i).copy();
            }
            personalityInventories.put(serverVictim.getUUID(), inventory);

            // 保存ender箱
            PlayerEnderChestContainer enderChest = serverVictim.getEnderChestInventory();
            ItemStack[] enderChestItems = new ItemStack[27];
            for (int i = 0; i < 27; i++) {
                enderChestItems[i] = enderChest.getItem(i).copy();
            }
            personalityEnderChests.put(serverVictim.getUUID(), enderChestItems);

            return false;
        });

        // 监听选择逻辑
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                var component = SplitPersonalityComponent.KEY.get(player);

                if (component == null || component.getMainPersonality() == null)
                    continue;

                // 移除倒计时检查逻辑

                // 检查临时复活是否超时 (60秒 = 1200刻)
                if (component.getTemporaryRevivalStartTick() > 0 && player.level() != null) {

                    if (component.getTemporaryRevivalStartTick() == 1) {
                        // 超时，强制死亡
                        component.setTemporaryRevivalStartTick(-1); // 防止重复杀死
                        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                            ServerPlayNetworking.send(player, new SplitBackCamera());
                            component.reset();
                            WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(player.level());
                            modifierComponent.removeModifier(player.getUUID(), SEModifiers.SPLIT_PERSONALITY);
                            GameFunctions.killPlayer(player, true, null);
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component
                                            .translatable("msg.stupid_express.split_personality.almostdead")
                                            .withStyle(ChatFormatting.RED),
                                    true);
                        }
                    }
                }
            }
        });
    }

    /**
     * 处理死亡选择的结果
     */
    public static void handleDeathChoicesPublic(ServerPlayer player, SplitPersonalityComponent component) {
        handleDeathChoices(player, component);
    }

    private static void handleDeathChoices(ServerPlayer player, SplitPersonalityComponent component) {
        var mainChoice = component.getMainPersonalityChoice();
        var secondChoice = component.getSecondPersonalityChoice();

        // 获取两个人格的玩家对象
        Player mainPlayer = player.level().getPlayerByUUID(component.getMainPersonality());
        Player secondPlayer = player.level().getPlayerByUUID(component.getSecondPersonality());

        if (mainPlayer == null || secondPlayer == null || !(mainPlayer instanceof ServerPlayer)
                || !(secondPlayer instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer mainServerPlayer = (ServerPlayer) mainPlayer;
        ServerPlayer secondServerPlayer = (ServerPlayer) secondPlayer;
        ServerPlayNetworking.send(mainServerPlayer, new SplitBackCamera());
        ServerPlayNetworking.send(secondServerPlayer, new SplitBackCamera());
        // 情况1：两个都选择欺骗 -> 直接死亡
        if (mainChoice == SplitPersonalityComponent.ChoiceType.BETRAY &&
                secondChoice == SplitPersonalityComponent.ChoiceType.BETRAY) {
            // 双双死亡，不需要做任何操作 (已经死了)
            GameFunctions.killPlayer(mainServerPlayer, true, null);
            GameFunctions.killPlayer(secondServerPlayer, true, null);
            component.reset();
            SplitPersonalityComponent.KEY.get(secondServerPlayer).reset();

            // 添加消息提示
            MutableComponent deathMessage = net.minecraft.network.chat.Component
                    .translatable("msg.stupid_express.split_personality.liebothdie").withStyle(ChatFormatting.RED);
            mainServerPlayer.displayClientMessage(deathMessage,
                    true);
            secondServerPlayer.displayClientMessage(deathMessage,
                    true);
            return;
        }

        // 情况2：一个欺骗一个奉献
        if ((mainChoice == SplitPersonalityComponent.ChoiceType.BETRAY
                && secondChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE) ||
                (mainChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE
                        && secondChoice == SplitPersonalityComponent.ChoiceType.BETRAY)) {

            ServerPlayer betrayerPlayer = mainChoice == SplitPersonalityComponent.ChoiceType.BETRAY ? mainServerPlayer
                    : secondServerPlayer;
            ServerPlayer sacrificePlayer = mainChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE
                    ? mainServerPlayer
                    : secondServerPlayer;

            // 欺骗者复活，奉献者保持死亡
            revivePlayer(betrayerPlayer, component);
            if (GameFunctions.isPlayerAliveAndSurvival(sacrificePlayer)) {
                GameFunctions.killPlayer(sacrificePlayer, true, null);
                SplitPersonalityComponent.KEY.get(secondServerPlayer).reset();
                component.reset();
            }
            if (component.getPlayer() == betrayerPlayer) {
                SplitPersonalityComponent.KEY.get(secondServerPlayer).reset();
                component.setDeath(false);
                component.reset();

            }

            // 添加消息提示
            betrayerPlayer.displayClientMessage(net.minecraft.network.chat.Component
                    .translatable("msg.stupid_express.split_personality.revive").withStyle(ChatFormatting.GREEN), true);
            sacrificePlayer.displayClientMessage(net.minecraft.network.chat.Component
                    .translatable("msg.stupid_express.split_personality.donatedied").withStyle(ChatFormatting.RED),
                    true);
            return;
        }

        // 情况3：两个都选择奉献 -> 两个都复活，但时间只有60秒
        if (mainChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE &&
                secondChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE) {

            revivePlayer(mainServerPlayer, component);
            revivePlayer(secondServerPlayer, component);
            if (component.getPlayer() == secondServerPlayer) {
                component.setDeath(false);
            }
            if (component.getPlayer() == mainServerPlayer) {
                component.setDeath(false);
            }

            // 设置临时复活开始时间（使用自定义tick计数器）
            var mainComp = SplitPersonalityComponent.KEY.get(mainServerPlayer);
            var secondComp = SplitPersonalityComponent.KEY.get(secondServerPlayer);

            if (mainComp != null)
                mainComp.setTemporaryRevivalStartTick(1200);
            if (secondComp != null)
                secondComp.setTemporaryRevivalStartTick(1200);

            mainServerPlayer.setGameMode(GameType.ADVENTURE);
            secondServerPlayer.setGameMode(GameType.ADVENTURE);

            // 添加消息提示
            MutableComponent reviveMessage = Component.translatable("msg.stupid_express.split_personality.reviveboth");
            mainServerPlayer.displayClientMessage(reviveMessage, true);
            secondServerPlayer.displayClientMessage(reviveMessage, true);
            return;
        }
    }

    /**
     * 复活玩家并恢复库存
     */
    private static void revivePlayer(ServerPlayer player, SplitPersonalityComponent component) {
        // 复活玩家
        player.setHealth(player.getMaxHealth());

        // 消除所有负面效果
        player.removeAllEffects();

        // 清空并恢复库存
        player.getInventory().clearContent();
        if (personalityInventories.containsKey(player.getUUID())) {
            ItemStack[] inventory = personalityInventories.get(player.getUUID());
            for (int i = 0; i < Math.min(36, inventory.length); i++) {
                ItemStack item = inventory[i];
                if (item != null) {
                    player.getInventory().setItem(i, item.copy());
                }
            }
        }

        // 恢复ender箱
        if (personalityEnderChests.containsKey(player.getUUID())) {
            ItemStack[] enderChestItems = personalityEnderChests.get(player.getUUID());
            PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
            for (int i = 0; i < Math.min(27, enderChestItems.length); i++) {
                ItemStack item = enderChestItems[i];
                if (item != null) {
                    enderChest.setItem(i, item.copy());
                }
            }
        }

        // 发送重生包到客户端
        player.setRespawnPosition(player.level().dimension(), player.blockPosition(), 0f, true, false);
    }

    /**
     * 获取另一个人格的玩家
     */
    public static ServerPlayer getOtherPersonality(ServerPlayer player) {
        var component = SplitPersonalityComponent.KEY.get(player);
        if (component == null || component.getMainPersonality() == null)
            return null;

        UUID otherPersonalityUUID;
        if (component.isMainPersonality()) {
            otherPersonalityUUID = component.getSecondPersonality();
        } else {
            otherPersonalityUUID = component.getMainPersonality();
        }

        return (ServerPlayer) player.level().getPlayerByUUID(otherPersonalityUUID);
    }

    /**
     * 检查玩家是否是观察者（未活跃的人格）
     */
    public static boolean isObserver(ServerPlayer player) {
        var component = SplitPersonalityComponent.KEY.get(player);
        if (component == null)
            return false;
        return !component.isCurrentlyActive();
    }

    /**
     * 清理库存数据
     */
    public static void cleanupInventoryData(UUID uuid) {
        personalityInventories.remove(uuid);
        personalityEnderChests.remove(uuid);
    }
}
