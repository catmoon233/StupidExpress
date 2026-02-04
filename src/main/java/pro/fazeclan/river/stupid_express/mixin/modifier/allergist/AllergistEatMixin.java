package pro.fazeclan.river.stupid_express.mixin.modifier.allergist;

import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.allergist.cca.AllergistComponent;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(Player.class)
public abstract class AllergistEatMixin extends LivingEntity {

    protected AllergistEatMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = {"eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V",
                    shift = At.Shift.AFTER
            )}
    )
    private void allergistConsume(@NotNull Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isClientSide) return;

        Player player = (Player) (Object) this;
        AllergistComponent allergist = AllergistComponent.KEY.get(player);

        if (!allergist.isAllergist()) return;

        // Random effect: 33% nothing, 33% slowness 2 for 5s, 33% speed 2 for 2s, 1% death
        double random = ThreadLocalRandom.current().nextDouble() * 100;

        if (random < 33) {
            // Nothing happens
            return;
        } else if (random < 66) {
            // Slowness 2 for 5 seconds (100 ticks)
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                    100,
                    1, true, false));
            
            player.displayClientMessage(
                    Component.translatable(
                            "hud.stupid_express.allergist.slowness"
                    ).withColor(SEModifiers.ALLERGIST.color()),
                    true
            );
        } else if (random < 99) {
            // Speed 2 for 2 seconds (40 ticks)
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                    40,
                    1, true, false));

            player.displayClientMessage(
                    Component.translatable(
                            "hud.stupid_express.allergist.speed_boost"
                    ).withColor(SEModifiers.ALLERGIST.color()),
                    true
            );
        } else {
            // Death
            GameFunctions.killPlayer(player, true, null, StupidExpress.id("allergist"));
            
            player.displayClientMessage(
                    Component.translatable(
                            "hud.stupid_express.allergist.death"
                    ).withColor(SEModifiers.ALLERGIST.color()),
                    true
            );
        }
    }
}
