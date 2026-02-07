package pro.fazeclan.river.stupid_express.client.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.client.network.SplitPersonalityClientPackets;

public class SplitPersonalityKeybinds {

    public static final KeyMapping SWITCH_PERSONALITY_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.stupid_express.switch_personality",
                    GLFW.GLFW_KEY_P,
                    "category.stupid_express"));

    public static void registerKeyPressCallbacks() {
        // 这个方法将在客户端初始化时被调用
    }

    public static void handleSwitchPersonalityKey(LocalPlayer player) {
        if (player == null)
            return;

        var component = SplitPersonalityComponent.KEY.get(player);
        if (component == null || component.getMainPersonality() == null)
            return;

        // 只允许在未死亡倒计时时切换
        if (!component.isInDeathCountdown() && component.canSwitch()) {
            // 发送切换包到服务器
            SplitPersonalityClientPackets.sendSwitchPacket();
            player.displayClientMessage(Component.literal("§e你已切换人格"), false);
        }
    }
}
