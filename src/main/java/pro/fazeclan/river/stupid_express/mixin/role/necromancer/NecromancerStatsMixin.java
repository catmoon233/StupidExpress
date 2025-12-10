package pro.fazeclan.river.stupid_express.mixin.role.necromancer;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.role.necromancer.cca.NecromancerComponent;

@Mixin(GameFunctions.class)
public class NecromancerStatsMixin {

    @Inject(
            method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("TAIL")
    )
    private static void stupidexpress$addKillStat(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {

        var component = GameWorldComponent.KEY.get(killer.level());
        if (component.canUseKillerFeatures(victim)) {
            var nc = NecromancerComponent.KEY.get(victim.level());
            nc.increaseAvailableRevives();
            nc.sync();
        }

    }

    @Inject(
            method = "tryResetTrain",
            at = @At("TAIL")
    )
    private static void stupidexpress$resetNecroStat(ServerLevel serverWorld, CallbackInfoReturnable<Boolean> cir) {

        var component = NecromancerComponent.KEY.get(serverWorld);
        component.reset();

    }

}
