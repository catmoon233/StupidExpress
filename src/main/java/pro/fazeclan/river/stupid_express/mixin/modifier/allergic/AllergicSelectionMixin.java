package pro.fazeclan.river.stupid_express.mixin.modifier.allergic;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.ModdedMurderGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.allergic.cca.AllergicComponent;
import pro.fazeclan.river.stupid_express.modifier.lovers.cca.LoversComponent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ModdedMurderGameMode.class)
public class AllergicSelectionMixin {
    @Inject(
            method = "initializeGame",
            at = @At("TAIL")
    )
    private void assignAllergic(ServerLevel serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayer> players, CallbackInfo ci) {

        if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(StupidExpress.ALLERGIC.identifier().getPath())) {
            return;
        }
        if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(StupidExpress.ALLERGIC.identifier().toString())) {
            return;
        }
        if (ThreadLocalRandom.current().nextInt(0, 3) != 0) {
            return;
        }

//        var innocentPlayers = players.stream().filter(gameWorldComponent::isInnocent).toList();

        var allergicPlayer = players.get(ThreadLocalRandom.current().nextInt(players.size() - 1));
        var targetComponent = LoversComponent.KEY.get(allergicPlayer);

        do {
            allergicPlayer = players.get(ThreadLocalRandom.current().nextInt(players.size() - 1));
            targetComponent = LoversComponent.KEY.get(allergicPlayer);
        } while (targetComponent.isLover());

        var allergicComponent = AllergicComponent.KEY.get(allergicPlayer);

        allergicComponent.setAllergic(allergicPlayer.getUUID());
        allergicComponent.setAllergyType(ThreadLocalRandom.current().nextBoolean() ? "food" : "drink");
        allergicComponent.sync();

        allergicPlayer.sendSystemMessage(
                Component.translatable(
                        "hud.allergic.notification",
                        allergicComponent.getAllergyType()
                ).withColor(StupidExpress.ALLERGIC_COLOR),
                true
        );
        for (ServerPlayer player : players) {
            if (GameWorldComponent.KEY.get(player.level()).getRole(player).identifier() != null) {
                if (GameWorldComponent.KEY.get(player.level()).getRole(player).identifier().equals(ResourceLocation.parse("harpysimpleroles:doctor"))) {
                    player.sendSystemMessage(
                            Component.translatable(
                                    "hud.allergic.doctor_heads_up" // This sends to players with a role from a different mod. I'm fucking genius.
                            ).withColor(StupidExpress.ALLERGIC_COLOR), // (Kid named genius:)
                            true
                    );
                }
            }
        }
    }
}
