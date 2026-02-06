package pro.fazeclan.river.stupid_express.client.mixin.modifier.split_personality;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;

@Mixin(RoleNameRenderer.class)
public abstract class SplitPersonalityHudMixin {

    @Shadow
    private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void splitPersonalityHud(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        var clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        var component = SplitPersonalityComponent.KEY.get(clientPlayer);
        if (component == null || component.getMainPersonality() == null || component.getSecondPersonality() == null) {
            return;
        }

        if (TMMClient.isPlayerSpectatingOrCreative()) {
            return;
        }

        context.pose().pushPose();

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int x = screenWidth - 200;
        int y = screenHeight - 100;

        // 绘制背景面板
        context.fill(x - 5, y - 5, x + 205, y + 95, 0x80000000);

        // 显示双重人格状态标题
        context.drawString(renderer, "§6双重人格", x, y, 0xFFFF55);

        // 显示当前活跃人格状态
        renderPersonalityStatus(context, renderer, component, clientPlayer, x, y + 12);

        // 显示自动切换倒计时
        renderSwitchTimer(context, renderer, component, x, y + 35);

        // 显示另一人格信息
        renderOtherPersonalityInfo(context, renderer, component, clientPlayer, x, y + 50);

        // 如果在死亡倒计时中，显示选择按钮
        if (component.isInDeathCountdown()) {
            renderDeathChoiceUI(context, renderer, component, x, y + 65);
        }

        context.pose().popPose();
    }

    private static void renderPersonalityStatus(GuiGraphics context, Font renderer, SplitPersonalityComponent component, LocalPlayer currentPlayer, int x, int y) {
        String statusText;
        int color;

        if (component.isCurrentlyActive()) {
            statusText = "状态: §e控制身体";
            color = 0xFFFF55;
        } else {
            statusText = "状态: §8旁观中";
            color = 0x888888;
        }

        context.drawString(renderer, statusText, x, y, color);
    }

    private static void renderSwitchTimer(GuiGraphics context, Font renderer, SplitPersonalityComponent component, int x, int y) {
        long remaining = component.canSwitch() ? 0 : (60000 - (System.currentTimeMillis() % 60000)) / 1000;
        if (remaining < 0) remaining = 0;

        String timerText = String.format("§9切换冷却: §f%d§9秒 (§fP§9键)", remaining);
        context.drawString(renderer, timerText, x, y, 0x99BBFF);
    }

    private static void renderOtherPersonalityInfo(GuiGraphics context, Font renderer, SplitPersonalityComponent component, LocalPlayer currentPlayer, int x, int y) {
        var clientLevel = currentPlayer.level();
        Player otherPersonality = component.isMainPersonality() ?
                clientLevel.getPlayerByUUID(component.getSecondPersonality()) :
                clientLevel.getPlayerByUUID(component.getMainPersonality());

        if (otherPersonality != null) {
            String otherStatus = component.isCurrentlyActive() ? "§8旁观中" : "§e控制中";
            String otherText = String.format("配对: §f%s §7%s", otherPersonality.getName().getString(), otherStatus);
            context.drawString(renderer, otherText, x, y, 0xAAAAAA);
        }
    }

    private static void renderDeathChoiceUI(GuiGraphics context, Font renderer, SplitPersonalityComponent component, int x, int y) {
        long remaining = component.getDeathCountdownRemainingTicks() / 20; // 转换为秒
        String countdownText = String.format("§c死亡倒计时: §f%d§c秒", remaining);
        context.drawString(renderer, countdownText, x, y, 0xFF5555);

        // 显示选择提示
        String choiceText = "§e[B]§e奉献 [V]§e欺骗";
        context.drawString(renderer, choiceText, x + 80, y, 0xFFFF55);

        // 显示已选择的状态
        if (component.getMainPersonalityChoice() != SplitPersonalityComponent.ChoiceType.NONE) {
            String choice = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ? "§2奉献" : "§c欺骗";
            context.drawString(renderer, String.format("§f你的选择: %s", choice), x, y + 12, 0xFFFFFF);
        }
    }
}
