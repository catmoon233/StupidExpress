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

        if (TMMClient.isPlayerSpectatingOrCreative() && component.isDeath()) {
            return;
        }

        context.pose().pushPose();

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int x = screenWidth - 200;
        int y = screenHeight - 120;

        // 绘制背景面板
        context.fill(x - 5, y - 5, x + 205, y + 110, 0x80000000);

        // 显示双重人格状态标题
        context.drawString(renderer, "§6双重人格", x, y, 0xFFFF55);

        // 显示当前活跃人格状态
        renderPersonalityStatus(context, renderer, component, clientPlayer, x, y + 12);

        // 显示自动切换倒计时
        renderSwitchTimer(context, renderer, component, x, y + 35);

        // 显示复活倒计时
        renderRevivalTimer(context, renderer, component, x, y + 50);

        // 显示另一人格信息
        renderOtherPersonalityInfo(context, renderer, component, clientPlayer, x, y + 65);

        // 显示当前选择状态
        renderChoiceStatus(context, renderer, component, x, y + 80);

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
        long remaining = component.canSwitch() ? 0 : (1200-(component.getBaseTickCounter()))/20;
        if (remaining < 0) remaining = 0;

        String timerText = String.format("§9切换冷却: §f%d§9秒 (§fY§9键)", remaining);
        context.drawString(renderer, timerText, x, y, 0x99BBFF);
    }

    private static void renderRevivalTimer(GuiGraphics context, Font renderer, SplitPersonalityComponent component, int x, int y) {
        if (component.getTemporaryRevivalStartTick()<=0)return;
        long remaining = ( component.getTemporaryRevivalStartTick()) / 20;
        if (remaining < 0) remaining = 0;

        String timerText = String.format("§a生命倒计时: §f%d§a秒", remaining);
        context.drawString(renderer, timerText, x, y, 0x55FF55);
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

    private static void renderChoiceStatus(GuiGraphics context, Font renderer, SplitPersonalityComponent component, int x, int y) {
        // 显示当前选择状态
        String choiceText;
        int choiceColor;
        
        if (component.isMainPersonality()) {
            choiceText = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 
                "§f当前选择: §2奉献" : "§f当前选择: §c欺骗";
            choiceColor = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 
                0x00FF00 : 0xFF0000;
        } else {
            choiceText = component.getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
                "§f当前选择: §2奉献" : "§f当前选择: §c欺骗";
            choiceColor = component.getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
                0x00FF00 : 0xFF0000;
        }

        context.drawString(renderer, choiceText, x, y, choiceColor);
        
        // 显示配对玩家的选择
//        String partnerChoiceText;
//        int partnerColor;
//
//        if (component.isMainPersonality()) {
//            partnerChoiceText = component.getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
//                "§f配对选择: §2奉献" : "§f配对选择: §c欺骗";
//            partnerColor = component.getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
//                0x00FF00 : 0xFF0000;
//        } else {
//            partnerChoiceText = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
//                "§f配对选择: §2奉献" : "§f配对选择: §c欺骗";
//            partnerColor = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ?
//                0x00FF00 : 0xFF0000;
//        }
        
 //       context.drawString(renderer, partnerChoiceText, x, y + 12, partnerColor);
    }
}
