package pro.fazeclan.river.stupid_express.client;

import dev.doctor4t.ratatouille.util.TextUtils;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.client.keybinds.SplitPersonalityKeybinds;
import pro.fazeclan.river.stupid_express.client.gui.SplitPersonalityDeathScreen;
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
    }

    private static void registerKeyEvents() {
        // 使用 Fabric Events 来处理按键按下事件
        if (Minecraft.getInstance().options != null) {
            // 按键处理会在客户端刻中进行
            net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
                var player = client.player;
                if (player == null)
                    return;

                while (SplitPersonalityKeybinds.SWITCH_PERSONALITY_KEY.consumeClick()) {
                    SplitPersonalityKeybinds.handleSwitchPersonalityKey(player);
                }

                // 检查是否需要打开死亡选择界面
                var component = SplitPersonalityComponent.KEY.get(player);
                if (component != null && component.isInDeathCountdown()) {
                    if (!(client.screen instanceof SplitPersonalityDeathScreen)) {
                        client.setScreen(new SplitPersonalityDeathScreen());
                    }
                }
            });
        }
    }
}
