package pro.fazeclan.river.stupid_express.client.gui.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

import java.awt.*;
import java.util.function.Consumer;

/**
 * 双重人格选择辅助类
 * 参考 SwapperScreenMixin 的模式，管理背包中的双重人格选择UI
 */
public class SplitPersonalityHelper {
    
    private final Player player;
    private SplitPersonalityComponent component;
    private SplitPersonalityComponent.ChoiceType currentChoice;
    private boolean isShowingChoice = false;
    private final Consumer<SplitPersonalityComponent.ChoiceType> onChoice;
    
    public SplitPersonalityHelper(Player player, Consumer<SplitPersonalityComponent.ChoiceType> onChoice) {
        this.player = player;
        this.onChoice = onChoice;
        this.component = SplitPersonalityComponent.KEY.get(player);
        // 初始化当前选择为默认值（奉献）
        this.currentChoice = SplitPersonalityComponent.ChoiceType.SACRIFICE;
    }
    
    /**
     * 获取当前选择的值
     */
    public SplitPersonalityComponent.ChoiceType getCurrentChoice() {
        return currentChoice;
    }
    
    /**
     * 设置当前选择
     */
    public void setCurrentChoice(SplitPersonalityComponent.ChoiceType choice) {
        this.currentChoice = choice;
    }
    
    /**
     * 提交选择
     */
    public void submitChoice() {
        if (component != null) {
            if (component.isMainPersonality()) {
                component.setMainPersonalityChoice(currentChoice);
            } else {
                component.setSecondPersonalityChoice(currentChoice);
            }
        }
        if (onChoice != null) {
            onChoice.accept(currentChoice);
        }
    }
    
    /**
     * 重置选择为默认值（奉献）
     */
    public void resetToDefault() {
        this.currentChoice = SplitPersonalityComponent.ChoiceType.SACRIFICE;
    }
    
    /**
     * 检查是否可以显示选择
     */
    public boolean canShowChoice() {
        component = SplitPersonalityComponent.KEY.get(player);
        return component != null && 
               component.getMainPersonality() != null &&
               !component.isDeath();
    }
    
    /**
     * 绘制选择提示信息
     */
    public void drawChoiceHint(GuiGraphics guiGraphics, int centerX, int centerY) {
        Minecraft client = Minecraft.getInstance();
        
        String titleText = "选择你的立场";
        int titleColor = 0xFFFF00;
        int titleY = centerY - 80;
        guiGraphics.drawCenteredString(client.font, titleText, centerX, titleY, titleColor);
        
        // 绘制当前选择指示
        String choiceText = "当前: " + getChoiceDisplayName(currentChoice);
        int choiceColor = currentChoice == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 0x00FF00 : 0xFF6600;
        guiGraphics.drawCenteredString(client.font, choiceText, centerX, centerY + 50, choiceColor);
        
        // 绘制操作提示
        String hintText = "点击按钮选择或使用方向键旋转";
        guiGraphics.drawCenteredString(client.font, hintText, centerX, centerY + 65, 0xAAAAAA);
    }
    
    /**
     * 获取选择类型的显示名称
     */
    public String getChoiceDisplayName(SplitPersonalityComponent.ChoiceType choice) {
        return switch (choice) {
            case SACRIFICE -> "奉献";
            case BETRAY -> "欺骗";
            case NONE -> "未知";
        };
    }
    
    /**
     * 旋转选择到下一个选项
     */
    public void rotateChoice() {
        this.currentChoice = switch (currentChoice) {
            case SACRIFICE -> SplitPersonalityComponent.ChoiceType.BETRAY;
            case BETRAY -> SplitPersonalityComponent.ChoiceType.SACRIFICE;
            case NONE -> SplitPersonalityComponent.ChoiceType.SACRIFICE;
        };
    }
    
    /**
     * 检查是否已选择
     */
    public boolean isChoiceMade() {
        return currentChoice != SplitPersonalityComponent.ChoiceType.NONE;
    }
}
