package pro.fazeclan.river.stupid_express.mixin.modifier.cursed;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.cursed.cca.CursedComponent;

@Mixin(GameFunctions.class)
public class CursedDeathMixin {

    // Handle deaths from GameFunctions.killPlayer (e.g., lovers, split personality, etc.)
    @Inject(
            method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD")
    )
    private static void onKillPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        if (!(victim instanceof ServerPlayer)) {
            return;
        }

        CursedComponent cursedComponent = CursedComponent.KEY.get(victim);

        if (cursedComponent.isCursed() && killer != null) {
            // Transfer curse
            cursedComponent.reset();
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(victim.level());
            worldModifierComponent.removeModifier(victim.getUUID(), SEModifiers.CURSED);

            CursedComponent killerCursedComponent = CursedComponent.KEY.get(killer);
            killerCursedComponent.setCursed(killer.getUUID());
            killerCursedComponent.sync();
            worldModifierComponent.addModifier(killer.getUUID(), SEModifiers.CURSED);
        }
    }
}