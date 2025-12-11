package pro.fazeclan.river.stupid_express.client.mixin.modifier.lovers;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.client.StupidExpressClient;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

@Mixin(RoleNameRenderer.class)
public abstract class LoversHudMixin {

    @Shadow
    private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void loversHud(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (StupidExpressClient.target == null) {
            return;
        }
        var component = LoversComponent.KEY.get(player);
        var targetComponent = LoversComponent.KEY.get(StupidExpressClient.target);
        if (component.isLover()
                && targetComponent.isLover()
                && !TMMClient.isPlayerSpectatingOrCreative()) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2.0f, context.guiHeight() / 2.0f - 35.0f, 0.0f);
            context.pose().scale(0.6f, 0.6f, 1.0f);

            //Component status = Component.translatable("tip.lovers.partner", component.getPartner(Minecraft.getInstance().player).getName());
            var status = Component.translatable("hud.lovers.partner");
            context.drawString(renderer, status, -renderer.width(status) / 2, 32, StupidExpress.LOVERS_COLOR);

            context.pose().popPose();
        }
    }

    @Inject(
            method = "renderHud",
            at = @At("TAIL")
    )
    private static void renderLovers(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (StupidExpressClient.target == null) {
            return;
        }
        var component = LoversComponent.KEY.get(StupidExpressClient.target);
        if (!component.isLover()) {
            return;
        }
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2.0f, context.guiHeight() / 2.0f - 35.0f, 0.0f);
            context.pose().scale(0.6f, 0.6f, 1.0f);

            Component name = Component.translatable(
                    "hud.lovers.in_love",
                    Minecraft.getInstance().level.getPlayerByUUID(component.getLover()).getName()
            );
            context.drawString(
                    renderer,
                    name,
                    -renderer.width(name) / 2,
                    32,
                    StupidExpress.LOVERS_COLOR | (int) (nametagAlpha * 255.0F) << 24
            );

            context.pose().popPose();
        }
    }

}
