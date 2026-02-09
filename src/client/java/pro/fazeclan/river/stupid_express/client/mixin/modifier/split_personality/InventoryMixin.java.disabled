package pro.fazeclan.river.stupid_express.client.mixin.modifier.split_personality;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.client.gui.widget.SplitPersonalityChoiceWidget;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

/**
 * Mixin for InventoryScreen to add SplitPersonality choice functionality
 * 参考 SwapperScreenMixin 的设计模式，在背包界面中集成双重人格选择功能
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    @Unique
    private Button sacrificeButton;
    @Unique
    private Button betrayButton;

    @Unique
    private SplitPersonalityComponent component;

    public InventoryMixin(InventoryMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    /**
     * 初始化时创建双重人格选择Widget
     */
    @Inject(method = "init", at = @At("HEAD"))
    private void stupid_express$onInit(CallbackInfo ci) {
        InventoryScreen self = (InventoryScreen) (Object) this;

        Player player = this.minecraft.player;
        this.component = SplitPersonalityComponent.KEY.get(player);

        // 只有在是双重人格且未死亡时才添加选择功能
        if (component != null && component.getMainPersonality() != null && component.getSecondPersonality() != null
                && !component.isDeath()) {
            SplitPersonalityChoiceWidget widgetFactory = new SplitPersonalityChoiceWidget(player);

            int buttonX = self.width / 2 - 110;
            int buttonY = self.height / 2 + 120;

            // 创建并添加两个独立的按钮
            this.sacrificeButton = widgetFactory.createSacrificeButton(buttonX, buttonY);
            this.betrayButton = widgetFactory.createBetrayButton(buttonX + 110, buttonY);

            this.addRenderableWidget(this.sacrificeButton);
            this.addRenderableWidget(this.betrayButton);
        }
    }

    /**
     * 渲染时显示选择提示文本和当前选择状态
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void stupid_express$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        InventoryScreen self = (InventoryScreen) (Object) this;

        Player player = this.minecraft.player;
        if (component == null) {
            component = SplitPersonalityComponent.KEY.get(player);
        }

        // 显示双重人格选择提示
        if (component != null && component.getMainPersonality() != null && !component.isDeath()) {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            int centerX = self.width / 2;
            int centerY = self.height / 2;

            // 标题文本
            MutableComponent titleText = Component
                    .translatable("screen.stupid_express.split_personality.inventory.choice.title")
                    .withStyle(ChatFormatting.GOLD);
            guiGraphics.drawCenteredString(client.font, titleText, centerX, centerY + 30, 0xFFFF00);

            // 按钮说明文本
            MutableComponent hintText = Component.translatable("or_2_argu",
                    Component.translatable("hud.stupid_express.split_personality.sacrifice")
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("hud.stupid_express.split_personality.betray")
                            .withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.GRAY);
            guiGraphics.drawCenteredString(client.font, hintText, centerX, centerY + 45, 0xAAAAAA);

            // 当前选择状态显示
            if (component.isMainPersonality()) {
                MutableComponent mainChoice = component
                        .getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE
                                ? Component.translatable("hud.stupid_express.split_personality.choice_now",
                                        Component
                                                .translatable("hud.stupid_express.split_personality.sacrifice")
                                                .withStyle(ChatFormatting.DARK_GREEN))
                                        .withStyle(ChatFormatting.WHITE)
                                : Component.translatable("hud.stupid_express.split_personality.choice_now",
                                        Component.translatable("hud.stupid_express.split_personality.betray")
                                                .withStyle(ChatFormatting.DARK_RED))
                                        .withStyle(ChatFormatting.WHITE);
                guiGraphics.drawCenteredString(client.font, mainChoice, centerX, centerY + 85, 0xFFFFFF);
            } else {
                MutableComponent secondChoice = component
                        .getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE
                                ? Component.translatable("hud.stupid_express.split_personality.choice_now",
                                        Component
                                                .translatable("hud.stupid_express.split_personality.sacrifice")
                                                .withStyle(ChatFormatting.DARK_GREEN))
                                        .withStyle(ChatFormatting.WHITE)
                                : Component.translatable("hud.stupid_express.split_personality.choice_now",
                                        Component.translatable("hud.stupid_express.split_personality.betray")
                                                .withStyle(ChatFormatting.DARK_RED))
                                        .withStyle(ChatFormatting.WHITE);
                guiGraphics.drawCenteredString(client.font, secondChoice, centerX, centerY + 85, 0xFFFFFF);
            }
        }
    }

    // 按钮点击事件由Button组件自动处理，无需额外的mixin注入
}