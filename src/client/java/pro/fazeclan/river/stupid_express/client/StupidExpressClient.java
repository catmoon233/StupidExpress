package pro.fazeclan.river.stupid_express.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import pro.fazeclan.river.stupid_express.client.gui.SplitPersonalityChoiceScreen;
import pro.fazeclan.river.stupid_express.client.gui.helper.SplitPersonalityHelper;
import pro.fazeclan.river.stupid_express.client.gui.widget.SplitPersonalityChoiceWidget;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.client.keybinds.SplitPersonalityKeybinds;
import pro.fazeclan.river.stupid_express.modifier.split_personality.SplitPersonalityHandler;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.network.SplitBackCamera;

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
        ClientPlayNetworking.registerGlobalReceiver(SplitBackCamera.TYPE, (payload, context) -> {
            Minecraft.getInstance().setCameraEntity(Minecraft.getInstance().player);
        });
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
        // 监听背包打开事件，根据CCA状态决定是否显示选择功能
//        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
//            if (screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
//                var player = client.player;
//                if (player == null) return;
//
//                var component = SplitPersonalityComponent.KEY.get(player);
//                // 只有在是双重人格且未死亡时才添加选择功能
//                if (component != null && component.getMainPersonality() != null && !component.isDeath()) {
//                    // 创建选择Widget
//                    int buttonX = scaledWidth / 2 - 110;
//                    int buttonY = scaledHeight / 2 + 60;
//
//                    var choiceWidget = new SplitPersonalityChoiceWidget(player, buttonX, buttonY);
//
//                    // 使用反射添加widget
//                    try {
//                        // 添加两个按钮到界面
//                        java.lang.reflect.Method addRenderableWidgetMethod = screen.getClass().getMethod("addRenderableWidget",
//                            net.minecraft.client.gui.components.Button.class);
//                        for (var button : choiceWidget.getButtons()) {
//                            addRenderableWidgetMethod.invoke(screen, button);
//                        }
//                    } catch (Exception e) {
//                        // 如果失败就忽略
//                        player.displayClientMessage(
//                            net.minecraft.network.chat.Component.literal("§c无法添加按钮: " + e.getMessage()), true);
//                    }
//                }
//            }
//        });
//
        // 监听背包渲染事件，添加选择提示信息
//        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_RENDER.register((client, screen, matrices, mouseX, mouseY) -> {
//            if (screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen) {
//                var player = client.player;
//                if (player == null) return;
//
//                var component = SplitPersonalityComponent.KEY.get(player);
//                // 显示选择提示文本
//                if (component != null && component.getMainPersonality() != null && !component.isDeath()) {
//                    int centerX = screen.width / 2;
//                    int centerY = screen.height / 2;
//
//                    String titleText = "§6双重人格选择";
//                    String hintText = "§e选择你的立场: 奉献(绿色) 或 欺骗(橙色)";
//
//                    matrices.drawCenteredString(client.font, titleText, centerX, centerY + 35, 0xFFFF00);
//                    matrices.drawCenteredString(client.font, hintText, centerX, centerY + 48, 0xAAAAAA);
//                }
//            }
//        });
    }
}
