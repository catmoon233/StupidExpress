package pro.fazeclan.river.stupid_express.client.mixin.role;

import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.cca.CustomWinnerComponent;

@Mixin(value = RoleAnnouncementTexts.RoleAnnouncementText.class, priority = 500)
public class CustomWinnerAnnouncementMixin {

    @Inject(
            method = "getEndText",
            at = @At("HEAD"),
            cancellable = true)
    private void getEndText(GameFunctions.WinStatus status, Component winner, CallbackInfoReturnable<Component> cir) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        var component = CustomWinnerComponent.KEY.get(level);
        if (!component.hasCustomWinner()) {
            return;
        }
        cir.setReturnValue(Component.translatable("announcement.win.stupid_express." + component.getWinningTextId()).withColor(component.getColor()));
        cir.cancel();
    }

}
