package pro.fazeclan.river.stupid_express.mixin.modifier.lovers;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

@Mixin(GameFunctions.class)
public class LoversDeathMixin {

    @Inject(
            method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V",
            at = @At("TAIL")
    )
    private static void bothLoversDie(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {

        LoversComponent component = LoversComponent.KEY.get(victim);

        if (!component.isLover()) {
            return;
        }

        var level = victim.level();
        var lover = level.getPlayerByUUID(component.getLover());
        if (lover != null) {
            GameFunctions.killPlayer(
                    lover,
                    true,
                    killer,
                    StupidExpress.id("broken_heart")
            );
        }

    }

    @Inject(method = "resetPlayer", at = @At("HEAD"))
    private static void stupidexpress$resetPlayer(ServerPlayer player, CallbackInfo ci) {
        var component = LoversComponent.KEY.get(player);
        component.reset();
        component.sync();
    }

}
