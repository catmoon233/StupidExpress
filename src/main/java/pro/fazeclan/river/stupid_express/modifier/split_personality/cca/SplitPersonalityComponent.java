package pro.fazeclan.river.stupid_express.modifier.split_personality.cca;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.UUID;

public class SplitPersonalityComponent implements AutoSyncedComponent {

    public static final ComponentKey<SplitPersonalityComponent> KEY =
            ComponentRegistry.getOrCreate(StupidExpress.id("split_personality"), SplitPersonalityComponent.class);

    private final Player player;

    private UUID splitPersonality;

    public SplitPersonalityComponent(Player player) {
        this.player = player;
    }

    public UUID getSplitPersonality() {
        return this.splitPersonality;
    }

    public void setSplitPersonality(UUID uuid) {
        this.splitPersonality = uuid;
        sync();
    }

    public boolean isSplitPersonality() {
        return this.splitPersonality != null && this.splitPersonality.equals(player.getUUID());
    }

    public void reset() {
        this.splitPersonality = null;
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.splitPersonality = tag.contains("split_personality") ? tag.getUUID("split_personality") : null;
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (this.splitPersonality != null) {
            tag.putUUID("split_personality", this.splitPersonality);
        }
    }
}
