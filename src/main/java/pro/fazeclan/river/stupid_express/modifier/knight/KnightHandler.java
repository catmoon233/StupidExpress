package pro.fazeclan.river.stupid_express.modifier.knight;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import pro.fazeclan.river.stupid_express.modifier.knight.cca.KnightComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class KnightHandler {
    // 跟踪侠客上一tick的位置，用于检测是否在移动
    private static final Map<UUID, PositionRecord> lastPositions = new HashMap<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0)
                return;

            List<ServerPlayer> knights = new ArrayList<>();
            List<ServerPlayer> targets = new ArrayList<>();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.isAlive())
                    continue;

                // 只有冒险模式的玩家才能作为交换目标
                // 并且玩家不能坐着
                if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !player.isPassenger()) {
                    targets.add(player);
                }

                KnightComponent component = KnightComponent.KEY.get(player);
                if (component.isKnight()) {
                    int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
                    if (light < 2) {
                        var motion = player.getDeltaMovement();
                        // 必须走路
                        if (motion.length() > 0.05) {
                            // 侠客也必须是冒险模式才能使用交换能力，并且不能坐着
                            if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !player.isPassenger()) {
                                knights.add(player);
                            }
                        }

                    }
                }
            }

            if (knights.isEmpty() || targets.size() < 2)
                return;

            Random random = new Random();
            for (ServerPlayer knight : knights) {

                // 10% chance per second
                if (random.nextDouble() <= 0.1) {
                    ServerPlayer target = targets.get(random.nextInt(targets.size()));
                    if (target == knight)
                        continue;

                    double kx = knight.getX();
                    double ky = knight.getY();
                    double kz = knight.getZ();

                    double tx = target.getX();
                    double ty = target.getY();
                    double tz = target.getZ();

                    knight.teleportTo(tx, ty, tz);
                    target.teleportTo(kx, ky, kz);
                }
            }
        });
    }

    // 位置记录类
    private static class PositionRecord {
        private final double x;
        private final double y;
        private final double z;

        PositionRecord(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        double x() {
            return x;
        }

        double y() {
            return y;
        }

        double z() {
            return z;
        }
    }
}