package pro.fazeclan.river.stupid_express.mixin.modifier.split_personality;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

@Mixin(TMM.class)
public class QuitVoiceMixin {
    @Redirect(method = "lambda$registerGlobalReceivers$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
    private static boolean quitVoice(ServerPlayer instance) {
        final var worldModifierComponent = WorldModifierComponent.KEY.get(instance.serverLevel());
        if (worldModifierComponent.isModifier(instance.getUUID(), SEModifiers.SPLIT_PERSONALITY)){
            if (!SplitPersonalityComponent.KEY.get(instance).isDeath()){
                return false;
            }
        }
        return instance.isSpectator();
    }

}
