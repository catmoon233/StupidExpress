package pro.fazeclan.river.stupid_express.mixin.accessor;

import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerPoisonComponent.class)
public interface PlayerPoisonComponentAccessor {
    @Accessor("poisonTicks")
    int getPoisonTicks();
}
