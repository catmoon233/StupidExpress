package pro.fazeclan.river.stupid_express.mixin.modifier.allergic;

import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.allergic.cca.AllergicComponent;

@Mixin(GameFunctions.class)
public class AllergicGameFunctionsMixin {

    @ModifyVariable(
            method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            argsOnly = true,
            name = "arg3"
    )
    private static ResourceLocation allergicDeath(ResourceLocation deathReason, Player victim, boolean spawnBody, Player killer) {
        AllergicComponent component = AllergicComponent.KEY.get(victim);

        if (!component.isAllergic()) {
            return deathReason;
        }

        if (killer == victim && deathReason == GameConstants.DeathReasons.POISON) {
            deathReason = StupidExpress.id("allergies");
        }
        return deathReason;
    }

}
