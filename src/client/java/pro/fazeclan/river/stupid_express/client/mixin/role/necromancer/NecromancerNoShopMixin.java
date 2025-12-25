package pro.fazeclan.river.stupid_express.client.mixin.role.necromancer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SERoles;

@Mixin(value = LimitedInventoryScreen.class)
public class NecromancerNoShopMixin {

    @Shadow
    @Final
    public LocalPlayer player;

    @Inject(
            method = "init",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/doctor4t/wathe/client/gui/screen/ingame/LimitedInventoryScreen;player:Lnet/minecraft/client/player/LocalPlayer;",
                    ordinal = 1,
                    opcode = Opcodes.GETFIELD,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void necromancerNoShop(CallbackInfo ci) {
        var level = this.player.level();
        var gameWorldComponent = GameWorldComponent.KEY.get(level);
        var config = StupidExpress.CONFIG;
        if (gameWorldComponent.isRole(this.player, SERoles.NECROMANCER) && !config.necromancerHasShop) {
            ci.cancel();
        }
    }

}

