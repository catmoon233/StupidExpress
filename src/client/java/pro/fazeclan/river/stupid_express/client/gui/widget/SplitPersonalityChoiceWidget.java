package pro.fazeclan.river.stupid_express.client.gui.widget;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.network.SplitPersonalityChoicePayload;

/**
 * 双重人格选择按钮工厂类
 * 用于创建独立的选择按钮
 */
public class SplitPersonalityChoiceWidget {
    
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    
    private final Player player;
    private final SplitPersonalityComponent component;
    
    public SplitPersonalityChoiceWidget(Player player) {
        this.player = player;
        this.component = SplitPersonalityComponent.KEY.get(player);
    }
    
    /**
     * 创建奉献按钮
     */
    public Button createSacrificeButton(int x, int y) {
        return Button.builder(Component.literal("奉献 (Sacrifice)"), button -> {
            submitChoice(SplitPersonalityComponent.ChoiceType.SACRIFICE);
        })
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }
    
    /**
     * 创建欺骗按钮
     */
    public Button createBetrayButton(int x, int y) {
        return Button.builder(Component.literal("欺骗 (Betray)"), button -> {
            submitChoice(SplitPersonalityComponent.ChoiceType.BETRAY);
        })
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }
    
    /**
     * 提交选择到服务器
     */
    private void submitChoice(SplitPersonalityComponent.ChoiceType choice) {
        if (component != null) {
            if (component.isMainPersonality()) {
                component.setMainPersonalityChoice(choice);
            } else {
                component.setSecondPersonalityChoice(choice);
            }
        }
        // 发送网络包到服务器
        int choiceValue = choice == SplitPersonalityComponent.ChoiceType.SACRIFICE ? 0 : 1;
        ClientPlayNetworking.send(new SplitPersonalityChoicePayload(choiceValue));
    }
    
    /**
     * 获取组件引用
     */
    public SplitPersonalityComponent getComponent() {
        return component;
    }
}