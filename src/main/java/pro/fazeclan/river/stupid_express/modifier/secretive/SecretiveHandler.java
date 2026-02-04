package pro.fazeclan.river.stupid_express.modifier.secretive;

import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import pro.fazeclan.river.stupid_express.modifier.secretive.cca.SecretiveComponent;

public class SecretiveHandler {
    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PlayerBodyEntity body) {
                if (body.getPlayerUuid() != null) {
                    ServerPlayer player = world.getServer().getPlayerList().getPlayer(body.getPlayerUuid());
                    if (player != null) {
                        SecretiveComponent component = SecretiveComponent.KEY.get(player);
                        if (component.isSecretive()) {
                            body.setCustomName(Component.literal("???"));
                            body.setCustomNameVisible(true);
                        }
                    }
                }
            }
        });
    }
}