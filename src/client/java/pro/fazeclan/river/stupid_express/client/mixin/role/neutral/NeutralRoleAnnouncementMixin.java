package pro.fazeclan.river.stupid_express.client.mixin.role.neutral;

import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.role.neutral.NeutralRoleWorldComponent;

@Mixin(value = RoleAnnouncementTexts.RoleAnnouncementText.class, priority = 500)
public class NeutralRoleAnnouncementMixin {

    @Inject(
            method = "getEndText",
            at = @At("HEAD"),
            cancellable = true)
    private void getEndText(GameFunctions.WinStatus status, Component winner, CallbackInfoReturnable<Component> cir) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        NeutralRoleWorldComponent component = NeutralRoleWorldComponent.KEY.get(level);
        if (!component.hasNeutralWinner()) {
            return;
        }
        cir.setReturnValue(component.getWinningText().winText);
        cir.cancel();
    }

}
