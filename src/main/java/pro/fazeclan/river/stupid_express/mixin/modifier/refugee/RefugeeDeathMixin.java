package pro.fazeclan.river.stupid_express.mixin.modifier.refugee;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.refugee.cca.RefugeeComponent;

@Mixin(GameFunctions.class)
public class RefugeeDeathMixin {

    @Inject(
            method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("TAIL")
    )
    private static void onRefugeeDeath(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        var level = victim.level();
        var worldModifierComponent = WorldModifierComponent.KEY.get(level);

        if (worldModifierComponent.isModifier(victim.getUUID(), SEModifiers.REFUGEE)) {
            var refugeeComponent = RefugeeComponent.KEY.get(level);
            refugeeComponent.addPendingRevival(victim.getUUID(), victim.getX(), victim.getY(), victim.getZ());
        }
    }
}