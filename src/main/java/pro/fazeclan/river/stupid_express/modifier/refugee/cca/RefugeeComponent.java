package pro.fazeclan.river.stupid_express.modifier.refugee.cca;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class RefugeeComponent implements ServerTickingComponent {

    public static final ComponentKey<RefugeeComponent> KEY =
            ComponentRegistry.getOrCreate(
                    StupidExpress.id("refugee"),
                    RefugeeComponent.class
            );

    private final Level level;
    private final List<RefugeeData> pendingRevivals = new ArrayList<>();

    public RefugeeComponent(Level level) {
        this.level = level;
    }

    @Override
    public void serverTick() {
        if (pendingRevivals.isEmpty()) {
            return;
        }

        long currentTime = level.getGameTime();
        Iterator<RefugeeData> iterator = pendingRevivals.iterator();

        while (iterator.hasNext()) {
            RefugeeData data = iterator.next();
            if (currentTime >= data.revivalTime) {
                revivePlayer(data);
                iterator.remove();
            }
        }
    }

    private void revivePlayer(RefugeeData data) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(data.uuid);
        if (player == null) {
            return; // Player is offline
        }

        // Teleport to death location
        player.teleportTo(serverLevel, data.x, data.y, data.z, player.getYRot(), player.getXRot());
        player.setGameMode(GameType.ADVENTURE);

        // Remove body entity
        List<PlayerBodyEntity> bodies = serverLevel.getEntitiesOfClass(PlayerBodyEntity.class, 
            new AABB(data.x - 2, data.y - 2, data.z - 2, data.x + 2, data.y + 2, data.z + 2));
        
        for (PlayerBodyEntity body : bodies) {
            if (body.getPlayerUuid().equals(data.uuid)) {
                body.remove(Entity.RemovalReason.DISCARDED);
                break;
            }
        }

        // Change role to LOOSE_END and remove REFUGEE modifier
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(serverLevel);
        gameWorldComponent.addRole(player, TMMRoles.LOOSE_END);
        
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(serverLevel);
        worldModifierComponent.removeModifier(player.getUUID(), SEModifiers.REFUGEE);

        // Effects and notifications
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30 * 20, 0, false, false));
        
        serverLevel.players().forEach(p -> {
            p.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            p.sendSystemMessage(Component.translatable("hud.stupid_express.refugee.revived", player.getDisplayName()), true);
        });
    }

    public void addPendingRevival(UUID uuid, double x, double y, double z) {
        // 2 minutes = 120 seconds = 2400 ticks
        long revivalTime = level.getGameTime() + 2400;
        pendingRevivals.add(new RefugeeData(uuid, revivalTime, x, y, z));
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        pendingRevivals.clear();
        if (tag.contains("pending_revivals")) {
            ListTag list = tag.getList("pending_revivals", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag item = list.getCompound(i);
                pendingRevivals.add(new RefugeeData(
                        item.getUUID("uuid"),
                        item.getLong("revival_time"),
                        item.getDouble("x"),
                        item.getDouble("y"),
                        item.getDouble("z")
                ));
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        ListTag list = new ListTag();
        for (RefugeeData data : pendingRevivals) {
            CompoundTag item = new CompoundTag();
            item.putUUID("uuid", data.uuid);
            item.putLong("revival_time", data.revivalTime);
            item.putDouble("x", data.x);
            item.putDouble("y", data.y);
            item.putDouble("z", data.z);
            list.add(item);
        }
        tag.put("pending_revivals", list);
    }

    private static class RefugeeData {
        final UUID uuid;
        final long revivalTime;
        final double x, y, z;

        RefugeeData(UUID uuid, long revivalTime, double x, double y, double z) {
            this.uuid = uuid;
            this.revivalTime = revivalTime;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}