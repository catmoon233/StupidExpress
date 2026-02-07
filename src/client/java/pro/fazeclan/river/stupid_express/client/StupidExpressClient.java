package pro.fazeclan.river.stupid_express.client;

import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import pro.fazeclan.river.stupid_express.client.gui.SplitPersonalityChoiceScreen;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.client.keybinds.SplitPersonalityKeybinds;
import pro.fazeclan.river.stupid_express.client.gui.SplitPersonalityDeathScreen;
import pro.fazeclan.river.stupid_express.modifier.split_personality.SplitPersonalityHandler;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

public class StupidExpressClient implements ClientModInitializer {

    public static Player target;
    public static PlayerBodyEntity targetBody;

    @Override
    public void onInitializeClient() {

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipFlag, list) -> {
            if (itemStack.is(SEItems.JERRY_CAN))
                list.addAll(TextUtils.getTooltipForItem(itemStack.getItem(), Style.EMPTY.withColor(8421504)));
            if (itemStack.is(SEItems.LIGHTER))
                list.addAll(TextUtils.getTooltipForItem(itemStack.getItem(), Style.EMPTY.withColor(8421504)));
        });

        // 初始化按键绑定
        SplitPersonalityKeybinds.registerKeyPressCallbacks();

        // 注册按键事件监听
        registerKeyEvents();
        
        // 注册网络接收器
        registerClientNetworkReceivers();
        
        // 注册背包界面事件
        registerInventoryEvents();
    }

    private static void registerKeyEvents() {
        // 使用 Fabric Events 来处理按键按下事件
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            var player = client.player;
            if (player == null)
                return;

            // 处理人格切换按键
            while (SplitPersonalityKeybinds.SWITCH_PERSONALITY_KEY.consumeClick()) {
                client.execute(() -> {

                    SplitPersonalityKeybinds.handleSwitchPersonalityKey(player);
                });
            }

            // 检查是否需要打开选择界面
            var component = SplitPersonalityComponent.KEY.get(player);
            if (component != null && component.getMainPersonality() != null) {
                // 可以在这里添加其他条件来触发选择界面
            }
        });
    }
    
    private static void registerClientNetworkReceivers() {
        // 客户端网络接收器注册
        // 实际的网络包处理已在SplitPersonalityPackets中注册
    }
    
    private static void registerInventoryEvents() {
        // 监听背包打开事件，在背包界面添加双重人格选择按钮
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
                var player = client.player;
                if (player == null) return;
                
                var component = SplitPersonalityComponent.KEY.get(player);
                if (component != null && component.getMainPersonality() != null) {
                    // 添加选择按钮
                    int buttonX = scaledWidth / 2 - 50;
                    int buttonY = scaledHeight / 2 + 60;
                    
                    var choiceButton = net.minecraft.client.gui.components.Button.builder(
                        net.minecraft.network.chat.Component.literal("人格选择"),
                        button -> {
                            // 打开选择界面
                            ServerPlayer otherPlayer = SplitPersonalityHandler.getOtherPersonality((ServerPlayer) player);
                            if (otherPlayer != null) {
                                client.setScreen(new SplitPersonalityChoiceScreen(otherPlayer));
                            }
                        }
                    ).bounds(buttonX, buttonY, 100, 20).build();
                    
                    screen.addRenderableWidget(choiceButton);
                }
            }
        });
    }
}
