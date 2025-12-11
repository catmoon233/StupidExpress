package pro.fazeclan.river.stupid_express.modifier.lovers.cca;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;

import java.util.UUID;

public class LoversComponent implements AutoSyncedComponent {

    public static final ComponentKey<LoversComponent> KEY =
            ComponentRegistry.getOrCreate(StupidExpress.id("lovers"), LoversComponent.class);

    private final Player player;

    @Getter
    @Setter
    private UUID lover;

    public LoversComponent(Player player) {
        this.player = player;
    }

    public void reset() {
        this.lover = null;
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public boolean won() {
        if (this.lover == null) {
            return false;
        }
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        var serverLevel = (ServerLevel) serverPlayer.level();
        if (serverLevel.getPlayers(GameFunctions::isPlayerAliveAndSurvival).size() != 2) {
            return false;
        }
        if (GameFunctions.isPlayerEliminated(this.player)) {
            return false;
        }
        var lover = serverLevel.getPlayerByUUID(this.lover);
        if (lover == null) {
            return false;
        }
        return !GameFunctions.isPlayerEliminated(lover);
    }

    public boolean isLover() {
        return this.lover != null && !this.lover.equals(UUID.fromString("4bdab31c-279a-4123-acac-9830ac57f5ff"));
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.lover = tag.contains("lover") ? tag.getUUID("lover") : null;
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        // W speed uuid button -->
        tag.putUUID("lover", this.lover != null ? this.lover : UUID.fromString("4bdab31c-279a-4123-acac-9830ac57f5ff"));
    }
}
