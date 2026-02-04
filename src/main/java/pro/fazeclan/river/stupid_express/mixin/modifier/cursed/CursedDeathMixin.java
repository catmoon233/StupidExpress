package pro.fazeclan.river.stupid_express.mixin.modifier.cursed;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.cursed.cca.CursedComponent;

@Mixin(ServerPlayer.class)
public class CursedDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        CursedComponent cursedComponent = CursedComponent.KEY.get(player);

        if (cursedComponent.isCursed()) {
            if (damageSource.getEntity() instanceof Player killer) {
                // Transfer curse
                cursedComponent.reset();
                WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
                worldModifierComponent.removeModifier(player.getUUID(), SEModifiers.CURSED);

                CursedComponent killerCursedComponent = CursedComponent.KEY.get(killer);
                killerCursedComponent.setCursed(killer.getUUID());
                killerCursedComponent.sync();
                worldModifierComponent.addModifier(killer.getUUID(), SEModifiers.CURSED);
            }
        }
    }
}