package pro.fazeclan.river.stupid_express.client.mixin.modifier.split_personality;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.agmas.harpymodloader.component.WorldModifierComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin {
    @Shadow
    public abstract boolean same(KeyMapping other);

    @Unique
    private boolean shouldSuppressKey() {
        if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRunning() && !TMMClient.isPlayerAliveAndInSurvival() && WorldModifierComponent.KEY.get(Minecraft.getInstance().player.level()).isModifier(Minecraft.getInstance().player, SEModifiers.SPLIT_PERSONALITY)) {

            final var splitPersonalityComponent = SplitPersonalityComponent.KEY.get(Minecraft.getInstance().player);
            if (splitPersonalityComponent == null || splitPersonalityComponent.getMainPersonality() ==null || splitPersonalityComponent.getSecondPersonality() ==null )return false;
            if (splitPersonalityComponent.getTemporaryRevivalStartTick()>0)return false;
            if (splitPersonalityComponent.isCurrentlyActive())return false;
                return this.same(Minecraft.getInstance().options.keySwapOffhand) ||
                        this.same(Minecraft.getInstance().options.keyJump) ||
                        this.same(Minecraft.getInstance().options.keyTogglePerspective) ||
                        this.same(Minecraft.getInstance().options.keyDrop) ||
                        this.same(Minecraft.getInstance().options.keyAttack) ||
                        this.same(Minecraft.getInstance().options.keyUp) ||
                        this.same(Minecraft.getInstance().options.keyRight) ||
                        this.same(Minecraft.getInstance().options.keyLeft) ||
                        this.same(Minecraft.getInstance().options.keyDown) ||
                        this.same(Minecraft.getInstance().options.keyUse) ||
                        this.same(Minecraft.getInstance().options.keyShift) ||
                        this.same(Minecraft.getInstance().options.keyAdvancements);

        }
        return false;
    }

    @ModifyReturnValue(method = "consumeClick", at = @At("RETURN"))
    private boolean noe$restrainWasPressedKeys(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean noe$restrainIsPressedKeys(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }

    @ModifyReturnValue(method = "matches", at = @At("RETURN"))
    private boolean noe$restrainMatchesKey(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }
}
