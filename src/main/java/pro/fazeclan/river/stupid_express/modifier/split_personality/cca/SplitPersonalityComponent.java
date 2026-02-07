package pro.fazeclan.river.stupid_express.modifier.split_personality.cca;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.UUID;

public class SplitPersonalityComponent implements AutoSyncedComponent, ServerTickingComponent , ClientTickingComponent {

    public static final ComponentKey<SplitPersonalityComponent> KEY = ComponentRegistry
            .getOrCreate(StupidExpress.id("split_personality"), SplitPersonalityComponent.class);

    public enum ChoiceType {
        NONE, // 未选择
        BETRAY, // 欺骗
        SACRIFICE // 奉献
    }

    @Override
    public void clientTick() {
        if (!isDeath){
            clientDo();
        }
    }

    private void clientDo() {
        // 预留客户端逻辑
    }
    private final Player player;

    // 两个人格的UUID
    private UUID mainPersonality;
    private UUID secondPersonality;

    // 当前活跃的人格 (控制身体)
    private UUID currentActivePerson;

    // 基础tick计数器
    private int baseTickCounter = 0;
    
    // 最后一次切换的tick值
    private int lastSwitchTick = 0;

    // 移除死亡倒计时相关方法

    public int getLastSwitchTick() {
        return lastSwitchTick;
    }

    public SplitPersonalityComponent setLastSwitchTick(int lastSwitchTick) {
        this.lastSwitchTick = lastSwitchTick;
        sync();
        return this;
    }

    public int getBaseTickCounter() {
        return baseTickCounter;
    }

    // 移除死亡倒计时相关getter/setter

    // 选择相关
    private ChoiceType mainPersonalityChoice = ChoiceType.SACRIFICE;  // 默认选择奉献
    private ChoiceType secondPersonalityChoice = ChoiceType.SACRIFICE;  // 默认选择奉献

    // 临时复活相关 (60秒限制，使用tick计数器)
    private int temporaryRevivalStartTick = -1;

    public int getTemporaryRevivalStartTick() {
        return temporaryRevivalStartTick;
    }

    public void setTemporaryRevivalStartTick(int temporaryRevivalStartTick) {
        this.temporaryRevivalStartTick = temporaryRevivalStartTick;
        sync();
    }

    public boolean isDeath() {
        return isDeath;
    }

    public SplitPersonalityComponent setDeath(boolean death) {
        isDeath = death;
        sync();
        return this;
    }

    private boolean isDeath = false;

    public SplitPersonalityComponent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    // ========== 基础Getter/Setter ==========
    public UUID getMainPersonality() {
        return mainPersonality;
    }

    public void setMainPersonality(UUID uuid) {
        this.mainPersonality = uuid;
        if (this.currentActivePerson == null) {
            this.currentActivePerson = uuid;
        }
        // 初始化时设置切换tick为当前计数器值
        if (this.lastSwitchTick == 0) {
            this.lastSwitchTick = this.baseTickCounter;
        }
        sync();
    }

    public UUID getSecondPersonality() {
        return secondPersonality;
    }

    public void setSecondPersonality(UUID uuid) {
        this.secondPersonality = uuid;
        // 初始化时设置切换tick为当前计数器值
        if (this.lastSwitchTick == 0) {
            this.lastSwitchTick = this.baseTickCounter;
        }
        sync();
    }

    public UUID getCurrentActivePerson() {
        return currentActivePerson;
    }

    public void setCurrentActivePerson(UUID uuid) {
        this.currentActivePerson = uuid;
        this.lastSwitchTick = this.baseTickCounter;
        sync();
    }

    public boolean isMainPersonality() {
        return mainPersonality != null && mainPersonality.equals(player.getUUID());
    }

    public boolean isSecondPersonality() {
        return secondPersonality != null && secondPersonality.equals(player.getUUID());
    }

    public boolean isCurrentlyActive() {
        return currentActivePerson != null && currentActivePerson.equals(player.getUUID());
    }

    // ========== 切换逻辑 ==========
    public boolean canSwitch() {
        // 必须在服务器端检查
        if (!(player instanceof ServerPlayer)) {
            return false;  // 客户端不应该检查这个
        }
        
        if (isDeath) {
            return false;  // 死亡时不能切换
        }
        if (mainPersonality == null || secondPersonality == null) {
            return false;  // 两个人格都初始化才能切换
        }
        
        // 使用自定义tick计数器进行时间检查
//        int ticksElapsed = baseTickCounter - lastSwitchTick;
        return isCurrentlyActive();  // 60秒 = 1200刻 (20刻/秒)
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player.equals(this.player);
    }

    public void switchPersonality() {
        if (!canSwitch()) {
            if (player instanceof ServerPlayer serverPlayer) {
                int ticksLeft = 1200 - (baseTickCounter - lastSwitchTick);
                if (ticksLeft > 0) {
                    double secondsLeft = ticksLeft / 20.0;
                    serverPlayer.displayClientMessage(
                        Component.literal("§c还需等待 " + String.format("%.1f", secondsLeft) + " 秒才能切换"), 
                        true
                    );
                }
            }
            return;
        }

        // 必须在服务器端执行
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 计算新的活跃人格（切换控制权）
        UUID newActivePerson = currentActivePerson.equals(mainPersonality) ? secondPersonality : mainPersonality;
        
        // 获取另一个人格的玩家
        Player otherPlayer = serverPlayer.serverLevel().getPlayerByUUID(
                currentActivePerson.equals(mainPersonality) ? secondPersonality : mainPersonality);

        // 确保另一个玩家存在且是服务器玩家
        if (otherPlayer == null || !(otherPlayer instanceof ServerPlayer otherServerPlayer)) {
            return;
        }


        // 更新切换时间（使用自定义tick计数器）
        int currentTick = 0;

        // 更新当前人格的组件
        this.baseTickCounter = currentTick;
        this.lastSwitchTick = currentTick;
        this.currentActivePerson = newActivePerson;
        this.sync();

        // 更新另一个人格的组件
        var otherComponent = SplitPersonalityComponent.KEY.get(otherPlayer);
        if (otherComponent != null) {
            otherComponent.baseTickCounter = currentTick;
            otherComponent.lastSwitchTick = currentTick;
            otherComponent.currentActivePerson = newActivePerson;
            otherComponent.sync();
        }

        // 更新游戏模式
        updateGameModes(serverPlayer, otherServerPlayer, newActivePerson);
    }

    private void updateGameModes(ServerPlayer thisPlayer, ServerPlayer otherPlayer, UUID newActivePerson) {
        // 更新当前玩家的游戏模式
        if (newActivePerson.equals(thisPlayer.getUUID())) {
            // 当前玩家成为活跃人格
            thisPlayer.setGameMode(GameType.ADVENTURE);
            thisPlayer.setCamera(thisPlayer);
            thisPlayer.displayClientMessage(Component.literal("§e你已成为活跃人格，获得控制权"), false);
        } else {
            // 当前玩家成为观察者
            thisPlayer.setGameMode(GameType.SPECTATOR);
            thisPlayer.setCamera(otherPlayer);
            thisPlayer.displayClientMessage(Component.literal("§7你已失去控制权，成为观察者"), false);
        }

        // 更新另一个玩家的游戏模式
        if (newActivePerson.equals(otherPlayer.getUUID())) {
            // 另一个玩家成为活跃人格
            otherPlayer.setGameMode(GameType.ADVENTURE);
            otherPlayer.setCamera(otherPlayer);
            otherPlayer.displayClientMessage(Component.literal("§e你已成为活跃人格，获得控制权"), false);
        } else {
            // 另一个玩家成为观察者
            otherPlayer.setGameMode(GameType.SPECTATOR);
            otherPlayer.setCamera(thisPlayer);
            otherPlayer.displayClientMessage(Component.literal("§7你已失去控制权，成为观察者"), false);
        }
    }

    // ========== 死亡倒计时相关 ==========
    // 移除死亡倒计时检查方法

    // 移除死亡倒计时启动方法

    // 移除死亡倒计时结束方法

    // 移除死亡倒计时剩余时间方法


    public void setMainPersonalityChoice(ChoiceType choice) {
        this.mainPersonalityChoice = choice;
        sync();
    }

    public void setSecondPersonalityChoice(ChoiceType choice) {
        this.secondPersonalityChoice = choice;
        sync();
    }

    public ChoiceType getMainPersonalityChoice() {
        return mainPersonalityChoice;
    }

    public ChoiceType getSecondPersonalityChoice() {
        return secondPersonalityChoice;
    }

    public boolean bothMadeChoice() {
        return mainPersonalityChoice != ChoiceType.NONE && secondPersonalityChoice != ChoiceType.NONE;
    }

    // ========== 重置 ==========
    public void reset() {
        this.mainPersonality = null;
        this.secondPersonality = null;
        this.currentActivePerson = null;
        this.lastSwitchTick = 0;
        this.baseTickCounter = 0;
        this.mainPersonalityChoice = ChoiceType.NONE;
        this.secondPersonalityChoice = ChoiceType.NONE;
        this.temporaryRevivalStartTick = -1;
        this.isDeath = false;
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("main_personality")) {
            this.mainPersonality = tag.getUUID("main_personality");
        }else this.mainPersonality = null;
        if (tag.contains("second_personality")) {
            this.secondPersonality = tag.getUUID("second_personality");
        }else this.secondPersonality = null;
        if (tag.contains("current_active")) {
            this.currentActivePerson = tag.getUUID("current_active");
        }else this.currentActivePerson = null;
        // 支持新的游戏刻格式
        if (tag.contains("last_switch_tick")) {
            this.lastSwitchTick = tag.getInt("last_switch_tick");
        } else if (tag.contains("last_switch_time")) {
            // 向后兼容：如果只有旧的毫秒时间戳，转换为刻
            this.lastSwitchTick = 0;  // 重置为0，视为初始状态
        }
        // 移除死亡倒计时NBT读取
        if (tag.contains("main_choice")) {
            this.mainPersonalityChoice = ChoiceType.valueOf(tag.getString("main_choice"));
        }
        if (tag.contains("second_choice")) {
            this.secondPersonalityChoice = ChoiceType.valueOf(tag.getString("second_choice"));
        }
        if (tag.contains("is_death")) {
            this.isDeath = tag.getBoolean("is_death");
        }
        // 支持新的游戏刻格式
        if (tag.contains("temp_revival_start_tick")) {
            this.temporaryRevivalStartTick = tag.getInt("temp_revival_start_tick");
        } else if (tag.contains("temp_revival_start")) {
            // 向后兼容
            this.temporaryRevivalStartTick = -1;
        }
        if (tag.contains("base_tick_counter")){
            this.baseTickCounter = tag.getInt("base_tick_counter");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (this.mainPersonality != null) {
            tag.putUUID("main_personality", this.mainPersonality);
        }
        if (this.secondPersonality != null) {
            tag.putUUID("second_personality", this.secondPersonality);
        }
        if (this.currentActivePerson != null) {
            tag.putUUID("current_active", this.currentActivePerson);
        }
        tag.putInt("last_switch_time", this.lastSwitchTick);
        // 移除死亡倒计时NBT写入
        tag.putString("main_choice", this.mainPersonalityChoice.name());
        tag.putString("second_choice", this.secondPersonalityChoice.name());
        tag.putBoolean("is_death", this.isDeath);
        tag.putInt("temp_revival_start_tick", this.temporaryRevivalStartTick);
        tag .putInt("base_tick_counter", this.baseTickCounter);
    }

    @Override
    public void serverTick() {
        if (mainPersonality==null || secondPersonality==null){
            reset();
            return;
        }
        if (getTemporaryRevivalStartTick() >0){
            temporaryRevivalStartTick = temporaryRevivalStartTick-1;
        }
        if (getTemporaryRevivalStartTick()>0 )return;

        if (isDeath) {
            return;
        }
        
        // 每tick递增基础计数器
        baseTickCounter++;
        // 检查是否需要自动切换 (60秒 = 1200刻)
        if (mainPersonality != null && secondPersonality != null) {
            int ticksSinceLastSwitch = baseTickCounter - lastSwitchTick;
            if (ticksSinceLastSwitch >= 1200) {  // 60秒自动切换
                switchPersonality();
            }
        }
        
        // 正常模式下的人格状态管理
        if (currentActivePerson != null && player instanceof ServerPlayer serverPlayer) {
            handleNormalMode(serverPlayer);
        }
        if (baseTickCounter%20==0){
            sync();
        }

    }
    
    // 移除死亡倒计时模式处理方法
    
    private void handleNormalMode(ServerPlayer serverPlayer) {
        // 检查是否是非活跃人格
        if (!currentActivePerson.equals(player.getUUID())) {
            if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                serverPlayer.setGameMode(GameType.SPECTATOR);
            }

            // 寻找活跃人格并锁定视角
            Player activePlayer = serverPlayer.serverLevel().getPlayerByUUID(currentActivePerson);
            if (activePlayer != null && serverPlayer.getCamera() != activePlayer) {
                serverPlayer.setCamera(activePlayer);
            }
        } else {
            // 如果是活跃人格，确保不是旁观者模式
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                serverPlayer.setGameMode(GameType.SURVIVAL);
                serverPlayer.setCamera(serverPlayer);
            }
        }
    }
}
