package pro.fazeclan.river.stupid_express.modifier.split_personality.cca;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.UUID;

public class SplitPersonalityComponent implements AutoSyncedComponent , ServerTickingComponent {

    public static final ComponentKey<SplitPersonalityComponent> KEY =
            ComponentRegistry.getOrCreate(StupidExpress.id("split_personality"), SplitPersonalityComponent.class);

    public enum ChoiceType {
        NONE, // 未选择
        BETRAY, // 欺骗
        SACRIFICE // 奉献
    }

    private final Player player;

    // 两个人格的UUID
    private UUID mainPersonality;
    private UUID secondPersonality;

    // 当前活跃的人格 (控制身体)
    private UUID currentActivePerson;

    // 最后一次切换时间
    private long lastSwitchTime;

    // 死亡倒计时相关
    private boolean isInDeathCountdown = false;
    private long deathCountdownStart = -1;
    private ChoiceType mainPersonalityChoice = ChoiceType.NONE;
    private ChoiceType secondPersonalityChoice = ChoiceType.NONE;

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
        sync();
    }

    public UUID getSecondPersonality() {
        return secondPersonality;
    }

    public void setSecondPersonality(UUID uuid) {
        this.secondPersonality = uuid;
        sync();
    }

    public UUID getCurrentActivePerson() {
        return currentActivePerson;
    }

    public void setCurrentActivePerson(UUID uuid) {
        this.currentActivePerson = uuid;
        this.lastSwitchTime = System.currentTimeMillis();
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
        if (!isInDeathCountdown && System.currentTimeMillis() - lastSwitchTime >= 60000) {
            return true;
        }
        return false;
    }

    public void switchPersonality() {
        if (mainPersonality == null || secondPersonality == null) {
            return;
        }
        if (currentActivePerson.equals(mainPersonality)) {
            setCurrentActivePerson(secondPersonality);
        } else {
            setCurrentActivePerson(mainPersonality);
        }
    }

    // ========== 死亡倒计时相关 ==========
    public boolean isInDeathCountdown() {
        return isInDeathCountdown;
    }

    public void startDeathCountdown() {
        this.isInDeathCountdown = true;
        this.deathCountdownStart = System.currentTimeMillis();
        this.mainPersonalityChoice = ChoiceType.NONE;
        this.secondPersonalityChoice = ChoiceType.NONE;
        sync();
    }

    public void endDeathCountdown() {
        this.isInDeathCountdown = false;
        this.deathCountdownStart = -1;
        sync();
    }

    public long getDeathCountdownRemainingTicks() {
        if (!isInDeathCountdown) return 0;
        long elapsed = System.currentTimeMillis() - deathCountdownStart;
        long remaining = 60000 - elapsed; // 60秒 = 60000毫秒
        return Math.max(0, remaining / 50); // 转换为游戏刻 (1刻 = 50ms)
    }

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
        this.lastSwitchTime = 0;
        this.isInDeathCountdown = false;
        this.deathCountdownStart = -1;
        this.mainPersonalityChoice = ChoiceType.NONE;
        this.secondPersonalityChoice = ChoiceType.NONE;
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("main_personality")) {
            this.mainPersonality = tag.getUUID("main_personality");
        }
        if (tag.contains("second_personality")) {
            this.secondPersonality = tag.getUUID("second_personality");
        }
        if (tag.contains("current_active")) {
            this.currentActivePerson = tag.getUUID("current_active");
        }
        if (tag.contains("last_switch_time")) {
            this.lastSwitchTime = tag.getLong("last_switch_time");
        }
        if (tag.contains("in_death_countdown")) {
            this.isInDeathCountdown = tag.getBoolean("in_death_countdown");
        }
        if (tag.contains("death_countdown_start")) {
            this.deathCountdownStart = tag.getLong("death_countdown_start");
        }
        if (tag.contains("main_choice")) {
            this.mainPersonalityChoice = ChoiceType.valueOf(tag.getString("main_choice"));
        }
        if (tag.contains("second_choice")) {
            this.secondPersonalityChoice = ChoiceType.valueOf(tag.getString("second_choice"));
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
        tag.putLong("last_switch_time", this.lastSwitchTime);
        tag.putBoolean("in_death_countdown", this.isInDeathCountdown);
        tag.putLong("death_countdown_start", this.deathCountdownStart);
        tag.putString("main_choice", this.mainPersonalityChoice.name());
        tag.putString("second_choice", this.secondPersonalityChoice.name());
    }

    @Override
    public void serverTick() {
        // 检查是否需要自动切换 (60秒)
        if (!isInDeathCountdown && mainPersonality != null && secondPersonality != null) {
            if (canSwitch()) {
                switchPersonality();
            }
        }

        // 检查死亡倒计时是否结束
        if (isInDeathCountdown && getDeathCountdownRemainingTicks() <= 0) {
            endDeathCountdown();
        }
    }
}
