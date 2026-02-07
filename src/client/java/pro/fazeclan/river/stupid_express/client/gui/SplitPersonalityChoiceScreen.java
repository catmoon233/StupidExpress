package pro.fazeclan.river.stupid_express.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.client.network.SplitPersonalityClientPackets;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class SplitPersonalityChoiceScreen extends Screen {
    
    private static SplitPersonalityComponent.ChoiceType currentPlayerChoice = SplitPersonalityComponent.ChoiceType.SACRIFICE;
    private Player otherPlayer;
    
    public SplitPersonalityChoiceScreen(Player otherPlayer) {
        super(Component.literal("双重人格选择"));
        this.otherPlayer = otherPlayer;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 欺骗按钮
        this.addRenderableWidget(Button.builder(Component.literal("欺骗 (Betray)"), button -> {
            currentPlayerChoice = SplitPersonalityComponent.ChoiceType.BETRAY;
            sendChoiceToServer(SplitPersonalityComponent.ChoiceType.BETRAY);
            this.onClose();
        })
                .bounds(centerX - 105, centerY - 20, 100, 40)
                .build());

        // 奉献按钮 (默认选中)
        this.addRenderableWidget(Button.builder(Component.literal("奉献 (Sacrifice)"), button -> {
            currentPlayerChoice = SplitPersonalityComponent.ChoiceType.SACRIFICE;
            sendChoiceToServer(SplitPersonalityComponent.ChoiceType.SACRIFICE);
            this.onClose();
        })
                .bounds(centerX + 5, centerY - 20, 100, 40)
                .build());

        // 显示当前选择状态
        updateButtonStates();
    }

    private void updateButtonStates() {
        // 可以在这里添加按钮状态的视觉反馈
    }

    private void sendChoiceToServer(SplitPersonalityComponent.ChoiceType choice) {
        int choiceValue = choice == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 0 : 1;
        SplitPersonalityClientPackets.sendChoicePacket(choiceValue);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        
        // 显示配对玩家信息
        if (otherPlayer != null) {
            String partnerInfo = "你的配对: " + otherPlayer.getName().getString();
            guiGraphics.drawCenteredString(this.font, Component.literal(partnerInfo), 
                this.width / 2, this.height / 2 - 35, 0xAAAAAA);
        }
        
        // 显示当前选择
        String currentChoiceText = "当前选择: " + 
            (currentPlayerChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE ? "奉献" : "欺骗");
        guiGraphics.drawCenteredString(this.font, Component.literal(currentChoiceText),
            this.width / 2, this.height / 2 + 30, 
            currentPlayerChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 0x00FF00 : 0xFF0000);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // 允许按ESC关闭
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static SplitPersonalityComponent.ChoiceType getCurrentPlayerChoice() {
        return currentPlayerChoice;
    }

    public static void setCurrentPlayerChoice(SplitPersonalityComponent.ChoiceType choice) {
        currentPlayerChoice = choice;
    }
}