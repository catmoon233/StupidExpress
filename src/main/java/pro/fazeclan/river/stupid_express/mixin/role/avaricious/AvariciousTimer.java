package pro.fazeclan.river.stupid_express.mixin.role.avaricious;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import pro.fazeclan.river.stupid_express.SERoles;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.role.avaricious.AvariciousGoldHandler;

import java.util.Iterator;

@Mixin(MurderGameMode.class)
public class AvariciousTimer {

    @Inject(
            method = "tickServerGameLoop",
            at = @At("TAIL")
    )
    private void actionbar(
            ServerLevel serverWorld,
            GameWorldComponent gameWorldComponent,
            CallbackInfo ci
    ) {

        // Optional: stop when game ends
        if (gameWorldComponent.getGameStatus() != GameWorldComponent.GameStatus.ACTIVE)
            return;

        GameTimeComponent timeComponent = GameTimeComponent.KEY.get(serverWorld);
        long time = timeComponent.time;

        int remainder = (int) (time % AvariciousGoldHandler.TIMER_TICKS);
        int ticksRemaining = AvariciousGoldHandler.TIMER_TICKS - remainder;

        // Update once per second
        if (time % 20 != 0) return;

        for (ServerPlayer player : serverWorld.players()) {

            if (!gameWorldComponent.isRole(player, SERoles.AVARICIOUS)) continue;

            String text = "§6§lPayout in: " + (60 - (ticksRemaining / 20)) + "s";
            player.sendSystemMessage(
                    Component.translatable(
                            text
                    ),
                    true
            );
        }
    }

}
