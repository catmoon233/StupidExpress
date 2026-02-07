package pro.fazeclan.river.stupid_express.client.mixin.modifier.split_personality;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

@Mixin(AbstractClientPlayer.class)
public abstract class SplitPersonalitySkinMixin {

    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void onGetSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        var component = SplitPersonalityComponent.KEY.get(player);
        if (component != null && component.isMainPersonality()) {
            // 强制第一人格使用默认皮肤 (Steve)
            // 如果有自定义皮肤，可以在这里替换为自定义的 ResourceLocation
            cir.setReturnValue(DefaultPlayerSkin.get(player.getUUID()).texture());
        }
    }
}