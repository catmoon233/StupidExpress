package pro.fazeclan.river.stupid_express.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pro.fazeclan.river.stupid_express.client.network.SplitPersonalityClientPackets;

public class SplitPersonalityDeathScreen extends Screen {

    public SplitPersonalityDeathScreen() {
        super(Component.literal("双重人格死亡选择"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 欺骗按钮
        this.addRenderableWidget(Button.builder(Component.literal("欺骗 (Betray)"), button -> {
            SplitPersonalityClientPackets.sendChoicePacket(1); // 1 = BETRAY
            this.onClose();
        })
                .bounds(centerX - 105, centerY - 20, 100, 40)
                .build());

        // 奉献按钮
        this.addRenderableWidget(Button.builder(Component.literal("奉献 (Sacrifice)"), button -> {
            SplitPersonalityClientPackets.sendChoicePacket(0); // 0 = SACRIFICE
            this.onClose();
        })
                .bounds(centerX + 5, centerY - 20, 100, 40)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.literal("请做出你的选择..."), this.width / 2, this.height / 2 - 35,
                0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // 禁止按ESC关闭
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}