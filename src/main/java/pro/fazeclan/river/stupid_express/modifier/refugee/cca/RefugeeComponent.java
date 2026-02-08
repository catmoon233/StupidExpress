package pro.fazeclan.river.stupid_express.modifier.refugee.cca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.utils.RoleUtils;

public class RefugeeComponent implements AutoSyncedComponent, ServerTickingComponent {

    public static final ComponentKey<RefugeeComponent> KEY = ComponentRegistry.getOrCreate(
            StupidExpress.id("refugee"),
            RefugeeComponent.class);

    public HashMap<UUID, PlayerStatsBeforeRefugee> players_stats = new HashMap<>();

    private final Level level;
    private final List<RefugeeData> pendingRevivals = new ArrayList<>();
    public boolean isAnyRevivals = false;

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

        // 每20 tick（1秒）发送一次倒计时提示
        if (currentTime % 20 == 0) {
            sendCountdownMessages();
            sync();
        }
    }

    private void sendCountdownMessages() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long currentTime = level.getGameTime();
        for (RefugeeData data : pendingRevivals) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(data.uuid);
            if (player == null) {
                continue;
            }

            long ticksRemaining = data.revivalTime - currentTime;
            int secondsRemaining = (int) ((ticksRemaining + 19) / 20);

            // 只在特定时间点发送消息（60秒、30秒、10秒）
            if (secondsRemaining == 60 || secondsRemaining == 30 || secondsRemaining == 10) {
                player.sendSystemMessage(
                        Component.translatable("hud.stupid_express.refugee.countdown", secondsRemaining), true);
            }
        }
    }

    public void sync() {
        KEY.sync(this.level);
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
        player.getInventory().clearContent();

        // Change role to LOOSE_END and remove REFUGEE modifier
        RoleUtils.changeRole(player, TMMRoles.LOOSE_END, false);
        TMM.REPLAY_MANAGER.recordPlayerRevival(player.getUUID(), TMMRoles.LOOSE_END);

        TrainVoicePlugin.resetPlayer(player.getUUID());

        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(serverLevel);
        worldModifierComponent.removeModifier(player.getUUID(), SEModifiers.REFUGEE);

        // Effects and notifications
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30 * 20, 0, false, false));
        serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack(),
                "title @a title {\"translate\":\"title.stupid_express.refugee.active\",\"color\":\"dark_red\"}");
                
        serverLevel.players().forEach(p -> {
            p.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            p.addEffect(new MobEffectInstance(MobEffects.WEAVING, 120 * 20, 0, false, false));
            p.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);

            p.sendSystemMessage(Component.translatable("hud.stupid_express.refugee.revived", player.getDisplayName()),
                    true);
            p.playNotifySound(StupidExpress.SOUND_REGUGEE, SoundSource.AMBIENT, 0.5f, 1.0f);
        });
        if (!isAnyRevivals) {
            SavePlayersStats();
        }
        isAnyRevivals = true;
    }

    public void SavePlayersStats() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ServerPlayer> players = serverLevel.getServer().getPlayerList().getPlayers();
        players_stats.clear();
        for (var player : players) {
            if (GameFunctions.isPlayerAliveAndSurvival(player))
                players_stats.put(player.getUUID(), PlayerStatsBeforeRefugee.SaveFromPlayer(player));
        }
    }

    public void LoadPlayersStats() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ServerPlayer> players = serverLevel.getServer().getPlayerList().getPlayers();
        var gameWorldComponent = GameWorldComponent.KEY.get(level);
        var entities = serverLevel.getAllEntities();
        var bodies = new HashMap<UUID, PlayerBodyEntity>();
        for (var entity : entities) {
            if (entity instanceof PlayerBodyEntity body) {
                bodies.put(body.getPlayerUuid(), body);
            }
        }
        for (var player : players) {
            var r = gameWorldComponent.getRole(player);
            if (r != null) {
                if (r.identifier().getPath().equals(TMMRoles.LOOSE_END.identifier().getPath())) {
                    continue;
                }
            }
            var data = players_stats.get(player.getUUID());

            if (data != null) {
                PlayerStatsBeforeRefugee.LoadToPlayer(player, data, r);
                // 删除玩家尸体
                // List<PlayerBodyEntity> bodies
                var body = bodies.get(player.getUUID());
                if (body != null)
                    body.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        bodies.clear();
    }

    public void onLooseEndDeath(Player who) {
        if (!(who instanceof ServerPlayer sp)) {
            return;
        }
        var gameWorldComponent = GameWorldComponent.KEY.get(sp.level());
        var a = sp.getServer().getPlayerList().getPlayers().stream().anyMatch((p) -> {
            if (GameFunctions.isPlayerAliveAndSurvival(p) || p.getUUID().equals(who.getUUID())) {
                return false;
            }
            var r = gameWorldComponent.getRole(p);
            if (r != null) {
                if (r.identifier().getPath().equals(TMMRoles.LOOSE_END.identifier().getPath())) {
                    return true;
                }
            }
            return false;
        });
        if (a) {
            return;
        }
        StupidExpress.LOGGER.info("Try to restore player's stat");

        isAnyRevivals = false;
        LoadPlayersStats();
        players_stats.clear(); // 清空玩家位置信息，避免浪费资源
        sp.getServer().getCommands().performPrefixedCommand(sp.getServer().createCommandSourceStack(),
                "{\"translate\":\"title.stupid_express.refugee.died\",\"color\":\"gold\"}");

        sp.getServer().getPlayerList().getPlayers().forEach((p) -> {
            p.playNotifySound(SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            p.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 40, 0, false, false));
            if (p.hasEffect(MobEffects.WEAVING)) {
                p.removeEffect(MobEffects.WEAVING);
            }
            p.displayClientMessage(Component.translatable("gui.stupid_express.refugee.all_death"), true);
            StopSound(p, StupidExpress.SOUND_REGUGEE.getLocation(), SoundSource.AMBIENT);
        });
    }

    public static void StopSound(ServerPlayer serverPlayer, ResourceLocation resourceLocation,
            SoundSource soundSource) {
        ClientboundStopSoundPacket clientboundStopSoundPacket = new ClientboundStopSoundPacket(resourceLocation,
                soundSource);
        serverPlayer.connection.send(clientboundStopSoundPacket);
    }

    public void addPendingRevival(UUID uuid, double x, double y, double z) {
        // 2 minutes = 120 seconds = 2400 ticks
        long revivalTime = level.getGameTime() + 2400;
        pendingRevivals.add(new RefugeeData(uuid, revivalTime, x, y, z));
        this.sync();
    }

    public long getRevivalTime(UUID uuid) {
        for (RefugeeData data : pendingRevivals) {
            if (data.uuid.equals(uuid)) {
                return data.revivalTime;
            }
        }
        return -1;
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
                        item.getDouble("z")));
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

    public void reset() {
        this.players_stats.clear();
        this.isAnyRevivals = false;
        this.pendingRevivals.clear();
        this.sync();
    }
}