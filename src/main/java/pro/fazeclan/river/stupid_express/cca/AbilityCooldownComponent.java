package pro.fazeclan.river.stupid_express.cca;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

public class AbilityCooldownComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<AbilityCooldownComponent> KEY = ComponentRegistry.getOrCreate(
            StupidExpress.id("cooldown"),
            AbilityCooldownComponent.class
    );
    private final Player player;
    @Getter
    @Setter
    private int cooldown = 0;

    public boolean hasCooldown() {
        return this.cooldown > 0;
    }

    public AbilityCooldownComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        if (this.cooldown > 0) {
            --this.cooldown;

            this.sync();
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("cooldown", this.cooldown);
    }
}
