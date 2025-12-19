package pro.fazeclan.river.stupid_express.client.mixin.modifier.lovers;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.SERoles;
import pro.fazeclan.river.stupid_express.client.StupidExpressClient;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

@Mixin(RoleNameRenderer.class)
public abstract class LoversHudMixin {

    @Shadow
    private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void loversHud(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        var component = LoversComponent.KEY.get(player);
        if (component.isLover()
                && !TMMClient.isPlayerSpectatingOrCreative()) {
            context.pose().pushPose();

            if (Minecraft.getInstance().player.connection.getPlayerInfo(component.getLover()) == null) return;

            Component name = Component.translatable("hud.lovers.notification", Minecraft.getInstance().player.connection.getPlayerInfo(component.getLover()).getProfile().getName());
            PlayerFaceRenderer.draw(context,Minecraft.getInstance().player.connection.getPlayerInfo(component.getLover()).getSkin().texture(), 2, context.guiHeight()-14,12);
            context.drawString(renderer, name, 18, context.guiHeight()-12, SERoles.LOVERS.color());

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
        var lover = Minecraft.getInstance().level.getPlayerByUUID(component.getLover());
        if (lover == null) {
            return;
        }
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2.0f, context.guiHeight() / 2.0f - 35.0f, 0.0f);
            context.pose().scale(0.6f, 0.6f, 1.0f);

            Component name = Component.translatable(
                    "hud.lovers.in_love",
                    lover.getName()
            );

            context.drawString(
                    renderer,
                    name,
                    -renderer.width(name) / 2,
                    32,
                    SERoles.LOVERS.color() | (int) (nametagAlpha * 255.0F) << 24
            );

            context.pose().popPose();
        }
    }

}
