package pro.fazeclan.river.stupid_express.mixin.modifier.feather;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.spongepowered.asm.mixin.Mixin;

import pro.fazeclan.river.stupid_express.constants.SEModifiers;

@Mixin(value = Entity.class, priority = 1005)
public abstract class DoNotCollideWithFeatherMixin {

    @WrapMethod(method = "collidesWith")
    boolean doNotCollideWithPlayers(Entity other, Operation<Boolean> original) {
        Entity self = (Entity)(Object)this;
        if (other instanceof Player player && self instanceof Player player2) {
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
            if (worldModifierComponent.isModifier(player, SEModifiers.FEATHER) || worldModifierComponent.isModifier(player2, SEModifiers.FEATHER)) {
                return false;
            }
        }
        return original.call(other);
    }
}
