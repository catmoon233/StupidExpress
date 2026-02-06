package pro.fazeclan.river.stupid_express.mixin.modifier.split_personality;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

/**
 * 服务器端 - 禁止旁观者的移动
 * 确保旁观者在服务器端无法移动或交互
 */
@Mixin(Player.class)
public abstract class SplitPersonalityServerInputBlockMixin {

    @Inject(
            method = "aiStep()V",
            at = @At("HEAD")
    )
    void blockServerSideSplitPersonalityInput(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        var component = SplitPersonalityComponent.KEY.get(player);
        
        // 如果是旁观者，清除所有移动
        if (component != null && component.getMainPersonality() != null && !component.isCurrentlyActive()) {
            // 禁止移动
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = false;
            
            // 禁止飞行模式
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
            }
            
            // 同步到活跃人格的位置
            Player activePlayer = (Player) player.level().getPlayerByUUID(component.getCurrentActivePerson());
            if (activePlayer != null && activePlayer != player) {
                // 强制旁观者位置与活跃人格一致
                player.teleportTo(activePlayer.getX(), activePlayer.getY(), activePlayer.getZ());
                player.setXRot(activePlayer.getXRot());
                player.setYRot(activePlayer.getYRot());
            }
        }
    }
}
