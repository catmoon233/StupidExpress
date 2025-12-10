package pro.fazeclan.river.stupid_express.modifier.lovers.cca;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.UUID;

public class LoversModifierWorldComponent implements AutoSyncedComponent {

    public static final ComponentKey<LoversModifierWorldComponent> KEY =
            ComponentRegistry.getOrCreate(StupidExpress.id("lovers_modifier_world"), LoversModifierWorldComponent.class);

    private final Level level;

    @Getter
    @Setter
    private Player loverOne;
    @Getter
    @Setter
    private Player loverTwo;
    @Getter
    private boolean won = false;

    public LoversModifierWorldComponent(Level level) {
        this.level = level;
    }

    public void reset() {
        this.loverOne = null;
        this.loverTwo = null;
        this.won = false;
        sync();
    }

    public void sync() {
        KEY.sync(this.level);
    }

    public boolean won() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (serverLevel.getPlayers(GameFunctions::isPlayerAliveAndSurvival).size() != 2) {
            return false;
        }
        if (GameFunctions.isPlayerEliminated(this.loverOne)) {
            return false;
        }
        this.won = true;
        return !GameFunctions.isPlayerEliminated(this.loverTwo);
    }

    public boolean isLover(Player player) {
        return isLover(player.getUUID());
    }

    public boolean isLover(UUID uuid) {
        return (this.loverOne != null && this.loverOne.getUUID() == uuid)
                || (this.loverTwo != null && this.loverTwo.getUUID() == uuid);
    }

    public Player getPartner(Player player) {
        return getPartner(player.getUUID());
    }

    public Player getPartner(UUID uuid) {
        if (!isLover(uuid)) {
            return null;
        }

        return this.loverOne.getUUID() == uuid ? this.loverTwo : this.loverOne;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.loverOne = this.level.getPlayerByUUID(tag.getUUID("lover_one"));
        this.loverTwo = this.level.getPlayerByUUID(tag.getUUID("lover_two"));
        this.won = tag.getBoolean("lovers_win");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        // W speed uuid button -->
        tag.putUUID("lover_one", this.loverOne != null ? this.loverOne.getUUID() : UUID.fromString("4bdab31c-279a-4123-acac-9830ac57f5ff"));
        tag.putUUID("lover_two", this.loverTwo != null ? this.loverTwo.getUUID() : UUID.fromString("4bdab31c-279a-4123-acac-9830ac57f5ff"));
        tag.putBoolean("lovers_win", this.won);
    }
}
