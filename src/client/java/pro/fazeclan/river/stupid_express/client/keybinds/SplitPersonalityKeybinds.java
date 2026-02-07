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
        if (component == null) {
            player.displayClientMessage(Component.literal("§c还未初始化双重人格"), true);
            return;
        }
        
        if (component.getMainPersonality() == null) {
            player.displayClientMessage(Component.literal("§c双重人格未初始化"), true);
            return;
        }
        
        // 已经移除了死亡倒计时机制
        
        // 已死亡无法切换
        if (component.isDeath()) {
            player.displayClientMessage(Component.literal("§c已死亡，无法切换"), true);
            return;
        }

        // 发送切换请求到服务器
        // 服务器会验证冷却时间和其他条件
        SplitPersonalityClientPackets.sendSwitchPacket();
        player.displayClientMessage(Component.literal("§e正在切换人格..."), true);
    }
}
