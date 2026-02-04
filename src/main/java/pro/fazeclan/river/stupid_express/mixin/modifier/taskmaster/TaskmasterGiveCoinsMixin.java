package pro.fazeclan.river.stupid_express.mixin.modifier.taskmaster;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import org.agmas.harpymodloader.component.WorldModifierComponent;

@Mixin(PlayerMoodComponent.class)
public abstract class TaskmasterGiveCoinsMixin {

    @Shadow public abstract float getMood();
    @Shadow @Final private Player player;

    @Inject(method = "setMood", at = @At("HEAD"))
    void taskmasterGiveCoins(float mood, CallbackInfo ci) {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        PlayerShopComponent playerShop = PlayerShopComponent.KEY.get(player);
        var role = gameWorld.getRole(player);

        // Give coins when mood increases
        if (mood > getMood()) {
            if (role != null) {
                WorldModifierComponent modifier = WorldModifierComponent.KEY.get(player.level());
                if (modifier.isModifier(player, SEModifiers.TASKMASTER)) {
                    // Killers get 50, civilians get 25
                    if (!gameWorld.isInnocent(player) &&
                        !isNeutralRole(role)) {
                        playerShop.addToBalance(50);
                    } else {
                        playerShop.addToBalance(25);
                    }
                }
            }
        }
    }

    private boolean isNeutralRole(Role role) {
        // Check if role is a custom neutral role
        return role != null && (
            role.equals(SERoles.AMNESIAC) ||
            role.equals(SERoles.AVARICIOUS) ||
            role.equals(SERoles.ARSONIST) ||
            role.equals(SERoles.NECROMANCER) ||
            role.equals(SERoles.INITIATE)
        );
    }
}
