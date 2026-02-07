package pro.fazeclan.river.stupid_express.client.mixin.modifier.split_personality;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.client.gui.widget.SplitPersonalityChoiceWidget;

/**
 * Mixin for InventoryScreen to add SplitPersonality choice functionality
 * 参考 SwapperScreenMixin 的设计模式，在背包界面中集成双重人格选择功能
 */
@Mixin(LimitedInventoryScreen.class)
public abstract class InventoryScreenSplitPersonalityMixin extends LimitedHandledScreen<InventoryMenu> {


    
    @Unique
    private Button sacrificeButton;
    @Unique
    private Button betrayButton;
    
    @Unique
    private SplitPersonalityComponent component;

    public InventoryScreenSplitPersonalityMixin(InventoryMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    /**
     * 初始化时创建双重人格选择Widget
     */
    @Inject(method = "init", at = @At("TAIL"))
    private void stupid_express$onInit(CallbackInfo ci) {
        LimitedInventoryScreen self = (LimitedInventoryScreen) (Object) this;

        Player player = this.minecraft.player;
        this.component = SplitPersonalityComponent.KEY.get(player);
        
        // 只有在是双重人格且未死亡时才添加选择功能
        if (component != null && component.getMainPersonality() != null && !component.isDeath()) {
            SplitPersonalityChoiceWidget widgetFactory = new SplitPersonalityChoiceWidget(player);
            
            int buttonX = self.width / 2 - 110;
            int buttonY = self.height / 2 + 60;
            
            // 创建并添加两个独立的按钮
            this.sacrificeButton = widgetFactory.createSacrificeButton(buttonX, buttonY);
            this.betrayButton = widgetFactory.createBetrayButton(buttonX + 110, buttonY);
            
            self.addRenderableWidget(this.sacrificeButton);
            self.addRenderableWidget(this.betrayButton);
        }
    }
    
    /**
     * 渲染时显示选择提示文本和当前选择状态
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void stupid_express$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        LimitedInventoryScreen self = (LimitedInventoryScreen) (Object) this;

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
            String titleText = "§6□ 双重人格选择 □";
            guiGraphics.drawCenteredString(client.font, titleText, centerX, centerY + 30, 0xFFFF00);
            
            // 按钮说明文本
            String hintText = "§e奉献 (绿) 或 欺骗 (橙)";
            guiGraphics.drawCenteredString(client.font, hintText, centerX, centerY + 45, 0xAAAAAA);
            
            // 当前选择状态显示
            if (component.isMainPersonality()) {
                String mainChoice = component.getMainPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 
                    "§a主格: 奉献" : "§6主格: 欺骗";
                guiGraphics.drawCenteredString(client.font, mainChoice, centerX, centerY + 65, 0xFFFFFF);
            } else {
                String secondChoice = component.getSecondPersonalityChoice() == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 
                    "§a副格: 奉献" : "§6副格: 欺骗";
                guiGraphics.drawCenteredString(client.font, secondChoice, centerX, centerY + 65, 0xFFFFFF);
            }
        }
    }
    
    // 按钮点击事件由Button组件自动处理，无需额外的mixin注入
}