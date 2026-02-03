package pro.fazeclan.river.stupid_express.client.mixin.role.amnesiac;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SERoles;

import java.awt.Color;

@Mixin(value = TMMClient.class, priority = 500)
public class AmnesiacInstinctGlowMixin {

    // i'm thinking the amnesiac could potentially be useful for killers if they can coerce them to pick up a killer's body
    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void amnesiacHighlightToKillers(Entity target, CallbackInfoReturnable<Integer> cir) {
        var player = Minecraft.getInstance().player;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (!StupidExpress.CONFIG.rolesSection.amnesiacSection.amnesiacGlowsDifferently) {
            return;
        }
        if (!(target instanceof Player targettedPlayer)) {
            return;
        }
        if (!gameWorldComponent.isRole(targettedPlayer, SERoles.AMNESIAC)) {
            return;
        }
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            return;
        }
        if (!TMMClient.isInstinctEnabled()) {
            return;
        }
        cir.setReturnValue(SERoles.AMNESIAC.color());
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void amnesiacHighlightBodies(Entity target, CallbackInfoReturnable<Integer> cir) {
        var player = Minecraft.getInstance().player;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (!StupidExpress.CONFIG.rolesSection.amnesiacSection.bodiesGlowToAmnesiac) {
            return;
        }
        if (!(target instanceof PlayerBodyEntity)) {
            return;
        }
        if (!gameWorldComponent.isRole(player, SERoles.AMNESIAC)) {
            return;
        }
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            return;
        }
        cir.setReturnValue(SERoles.AMNESIAC.color());
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void fakeAmnesiacGreenGlow(Entity target, CallbackInfoReturnable<Integer> cir) {
        var player = Minecraft.getInstance().player;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (!(target instanceof Player targettedPlayer)) {
            return;
        }
        if (!gameWorldComponent.isRole(targettedPlayer, SERoles.AMNESIAC)) {
            return;
        }
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            return;
        }
        if (!TMMClient.isInstinctEnabled()) {
            return;
        }
        cir.setReturnValue(Color.GREEN.getRGB());
    }

}
