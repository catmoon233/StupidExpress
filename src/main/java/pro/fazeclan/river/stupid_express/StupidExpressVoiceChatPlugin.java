package pro.fazeclan.river.stupid_express;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

public class StupidExpressVoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return StupidExpress.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
    }

    public void paranoidEvent(MicrophonePacketEvent event) {

        VoicechatServerApi api = event.getVoicechat();
        ServerPlayer players = ((ServerPlayer) event.getSenderConnection().getPlayer().getPlayer());
        WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(players.serverLevel());
        final var splitPersonalityComponent = SplitPersonalityComponent.KEY.get(players);
        if (splitPersonalityComponent.getMainPersonality() == null
                || splitPersonalityComponent.getSecondPersonality() == null)
            return;

        // if (players.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {
        players.level().players().forEach((p) -> {
            if (p != players) {
                if (modifierComponent.isModifier(p, SEModifiers.SPLIT_PERSONALITY)) {
                    if (modifierComponent.isModifier(players, SEModifiers.SPLIT_PERSONALITY)) {
                        VoicechatConnection con = api.getConnectionOf(p.getUUID());
                        api.sendLocationalSoundPacketTo(con, event.getPacket().locationalSoundPacketBuilder()
                                .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                .distance((float) api.getVoiceChatDistance())
                                .build());
                    }
                } else {
                    if (p instanceof ServerPlayer serverPlayer) {
                        if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                            event.cancel();
                        }
                    }
                }
            }

        });
        // if (gameWorldComponent.isRole(p,
        // Noellesroles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)
        // && GameFunctions.isPlayerAliveAndSurvival(p)) {
        // if (players.distanceTo(p) <= api.getVoiceChatDistance()) {
        // VoicechatConnection con = api.getConnectionOf(p.getUuid());
        // api.sendLocationalSoundPacketTo(con,
        // event.getPacket().locationalSoundPacketBuilder()
        // .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
        // .distance((float)api.getVoiceChatDistance())
        // .build());
        // }
        // }
        // });
        // }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::paranoidEvent);
        VoicechatPlugin.super.registerEvents(registration);
    }
}
