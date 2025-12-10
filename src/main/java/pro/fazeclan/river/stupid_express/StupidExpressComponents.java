package pro.fazeclan.river.stupid_express;

import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversModifierWorldComponent;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.DousedPlayerComponent;
import pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent;
import pro.fazeclan.river.stupid_express.role.neutral.NeutralRoleWorldComponent;
import pro.fazeclan.river.stupid_express.role.neutral.cca.AbilityCooldownComponent;

public class StupidExpressComponents implements EntityComponentInitializer, WorldComponentInitializer {

    public StupidExpressComponents() {}

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, DousedPlayerComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(DousedPlayerComponent::new);
        registry.beginRegistration(Player.class, AbilityCooldownComponent.KEY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AbilityCooldownComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(NeutralRoleWorldComponent.KEY, NeutralRoleWorldComponent::new);
        registry.register(LoversModifierWorldComponent.KEY, LoversModifierWorldComponent::new);
        registry.register(NecromancerComponent.KEY, NecromancerComponent::new);
    }
}
