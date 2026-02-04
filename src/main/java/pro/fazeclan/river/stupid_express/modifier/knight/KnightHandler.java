package pro.fazeclan.river.stupid_express.modifier.knight;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;
import pro.fazeclan.river.stupid_express.modifier.knight.cca.KnightComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnightHandler {
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;

            List<ServerPlayer> knights = new ArrayList<>();
            List<ServerPlayer> targets = new ArrayList<>();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!player.isAlive()) continue;
                
                targets.add(player);
                
                KnightComponent component = KnightComponent.KEY.get(player);
                if (component.isKnight()) {
                    int light = player.level().getBrightness(LightLayer.BLOCK, player.blockPosition());
                    if (light < 2) {
                        knights.add(player);
                    }
                }
            }

            if (knights.isEmpty() || targets.size() < 2) return;

            Random random = new Random();
            for (ServerPlayer knight : knights) {
                // 30% chance per second
                if (random.nextDouble() < 0.3) {
                    ServerPlayer target = targets.get(random.nextInt(targets.size()));
                    if (target == knight) continue;

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
}