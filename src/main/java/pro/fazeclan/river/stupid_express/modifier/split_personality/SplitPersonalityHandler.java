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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.agmas.harpymodloader.component.WorldModifierComponent;

import pro.fazeclan.river.stupid_express.StupidExpress;
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
            ItemStack[] inventory = new ItemStack[36]; // 标准库存大小
            for (int i = 0; i < 36; i++) {
                inventory[i] = serverVictim.getInventory().getItem(i).copy();
            }
            personalityInventories.put(serverVictim.getUUID(), inventory);

            return handleDeathChoicesPublic(serverVictim, component);
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
    public static boolean handleDeathChoicesPublic(ServerPlayer player, SplitPersonalityComponent component) {
        var mainChoice = component.getMainPersonalityChoice();
        var secondChoice = component.getSecondPersonalityChoice();
        UUID p_au = component.getMainPersonality();
        UUID p_bu = component.getSecondPersonality();
        Player p_a = player.level().getPlayerByUUID(p_au);
        Player p_b = player.level().getPlayerByUUID(p_bu);
        if (!(p_a instanceof ServerPlayer p_sa))
            return true;
        if (!(p_b instanceof ServerPlayer p_sb))
            return true;
        int playerType = 0;
        if (p_au.equals(player.getUUID())) {
            playerType = 1; // 主人格
        } else if (p_bu.equals(player.getUUID())) {
            playerType = 2; // 副人格
        }
        if (playerType == 1) {
            var nComp = SplitPersonalityComponent.KEY.get(p_sb);
            boolean needDeath = handleDeathChoices(p_sb, nComp);
            if (needDeath) {
                p_sb.setGameMode(GameType.ADVENTURE);
                nComp.reset();
                GameFunctions.killPlayer(p_sb, false, player, StupidExpress.id("split_personality"));
            }else{
                p_sb.teleportTo(player.getX(), player.getY(), player.getZ());
                p_sb.setGameMode(GameType.ADVENTURE);
                revivePlayer(p_sb, nComp);
                // 复活
            }
        } else {
            var nComp = SplitPersonalityComponent.KEY.get(p_sa);
            boolean needDeath = handleDeathChoices(p_sa, nComp);
            if (needDeath) {
                p_sb.setGameMode(GameType.ADVENTURE);
                nComp.reset();
                GameFunctions.killPlayer(p_sa, false, player, StupidExpress.id("split_personality"));
            }else{
                p_sa.teleportTo(player.getX(), player.getY(), player.getZ());
                p_sa.setGameMode(GameType.ADVENTURE);
                revivePlayer(p_sa, nComp);
                // 复活
            }
        }

        return handleDeathChoices(player, component);
    }

    private static boolean handleDeathChoices(ServerPlayer player, SplitPersonalityComponent component) {
        var mainChoice = component.getMainPersonalityChoice();
        var secondChoice = component.getSecondPersonalityChoice();
        UUID p_a = component.getMainPersonality();
        UUID p_b = component.getSecondPersonality();
        int playerType = 0;
        if (p_a.equals(player.getUUID())) {
            playerType = 1; // 主人格
        } else if (p_b.equals(player.getUUID())) {
            playerType = 2; // 副人格
        }
        final var worldModifierComponent = WorldModifierComponent.KEY.get(player.serverLevel());
        ServerPlayNetworking.send(player, new SplitBackCamera());
        // 预留：都复活
        // 情况3：两个都选择奉献 -> 两个都复活，但时间只有60秒
        if (mainChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE &&
                secondChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE) {

            revivePlayer(player, component);
            component.setDeath(false);

            component.setTemporaryRevivalStartTick(1200);

            player.setGameMode(GameType.ADVENTURE);

            // 添加消息提示
            MutableComponent reviveMessage = Component.translatable("msg.stupid_express.split_personality.reviveboth");
            player.displayClientMessage(reviveMessage, true);
            return false;
        }

        // 删除控件
        worldModifierComponent.removeModifier(player.getUUID(), SEModifiers.SPLIT_PERSONALITY);

        // 情况1：两个都选择欺骗 -> 直接死亡
        if (mainChoice == SplitPersonalityComponent.ChoiceType.BETRAY &&
                secondChoice == SplitPersonalityComponent.ChoiceType.BETRAY) {
            component.reset();
            // 添加消息提示
            MutableComponent deathMessage = net.minecraft.network.chat.Component
                    .translatable("msg.stupid_express.split_personality.liebothdie").withStyle(ChatFormatting.RED);
            player.displayClientMessage(deathMessage,
                    true);
            return true;
        }

        // 情况2：一个欺骗一个奉献
        if ((mainChoice == SplitPersonalityComponent.ChoiceType.BETRAY
                && secondChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE)) {
            component.reset();
            if (playerType == 1) {
                revivePlayer(player, component);
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.stupid_express.split_personality.revive").withStyle(ChatFormatting.GREEN),
                        true);
                return false;
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.stupid_express.split_personality.donatedied").withStyle(ChatFormatting.RED),
                        true);
                return true;
            }

        }
        if (mainChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE
                && secondChoice == SplitPersonalityComponent.ChoiceType.BETRAY) {
            component.reset();
            if (playerType == 2) {
                revivePlayer(player, component);
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.stupid_express.split_personality.revive").withStyle(ChatFormatting.GREEN),
                        true);
                return false;
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .translatable("msg.stupid_express.split_personality.donatedied").withStyle(ChatFormatting.RED),
                        true);
                return true;
            }
        }
        return true;
    }

    /**
     * 复活玩家并恢复库存
     */
    private static void revivePlayer(ServerPlayer player, SplitPersonalityComponent component) {
        // 复活玩家
        player.setHealth(player.getMaxHealth());

        // 消除所有负面效果
        player.removeAllEffects();
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
