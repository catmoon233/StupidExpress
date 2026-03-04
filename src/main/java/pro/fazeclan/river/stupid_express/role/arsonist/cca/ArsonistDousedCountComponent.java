package pro.fazeclan.river.stupid_express.role.arsonist.cca;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

public class ArsonistDousedCountComponent implements AutoSyncedComponent {

    public static final ComponentKey<ArsonistDousedCountComponent> KEY = ComponentRegistry.getOrCreate(
            StupidExpress.id("arsonist_doused_count"),
            ArsonistDousedCountComponent.class);

    private final Level level;
    private int dousedCount = 0;

    public int getDousedCount() {
        return this.dousedCount;
    }

    public void setDousedCount(int count) {
        this.dousedCount = count;
    }

    public void incrementDousedCount() {
        this.dousedCount++;
    }

    public void resetDousedCount() {
        this.dousedCount = 0;
    }

    public ArsonistDousedCountComponent(Level level) {
        this.level = level;
        this.dousedCount = 0;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.dousedCount = tag.getInt("doused_count");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("doused_count", this.dousedCount);
    }

}
