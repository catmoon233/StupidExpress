package pro.fazeclan.river.stupid_express;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.minecraft.server.level.ServerPlayer;
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
        VoicechatConnection connection_s = event.getSenderConnection();
        VoicechatConnection connection_r = event.getReceiverConnection();
        if (connection_s == null || connection_s.getPlayer() == null) {
            return;
        }

        ServerPlayer sender = ((ServerPlayer) connection_s.getPlayer().getPlayer());
        WorldModifierComponent modifierComponent = WorldModifierComponent.KEY.get(sender.serverLevel());
        final var senderSpc = SplitPersonalityComponent.KEY.get(sender);

        if (senderSpc.getMainPersonality() != null
                && senderSpc.getSecondPersonality() != null) {
            if (modifierComponent.isModifier(sender, SEModifiers.SPLIT_PERSONALITY)) {
                if (!senderSpc.isDeath() && sender.isSpectator()) {
                    // sender 旁观，给双重人格发语音
                    sender.level().players().forEach((p) -> {
                        if (p != sender) {
                            if (modifierComponent.isModifier(p, SEModifiers.SPLIT_PERSONALITY)) {
                                {
                                    VoicechatConnection con = api.getConnectionOf(p.getUUID());
                                    api.sendLocationalSoundPacketTo(con,
                                            event.getPacket().locationalSoundPacketBuilder()
                                                    .position(api.createPosition(p.getX(), p.getY(), p.getZ()))
                                                    .distance((float) api.getVoiceChatDistance())
                                                    .build());
                                }
                            }
                        }
                    });
                    return;
                }
                // sender 非旁观，该咋处理咋处理
            }
        }

        if (connection_r != null && connection_r.getPlayer() != null) {
            // 有接收者
            ServerPlayer receiver = ((ServerPlayer) connection_r.getPlayer().getPlayer());
            final var receiverSpc = SplitPersonalityComponent.KEY.get(receiver);
            if (receiverSpc.getMainPersonality() != null
                    && receiverSpc.getSecondPersonality() != null) {
                if (modifierComponent.isModifier(receiver, SEModifiers.SPLIT_PERSONALITY)) {
                    // 如果接收的是双重人格
                    if (receiver.isSpectator()) {
                        // 如果此人还没死，且在旁观（等待切换）
                        if (!receiverSpc.isDeath() && sender.isSpectator()) {
                            // 拒绝来自旁观 sender 的语音
                            event.cancel();
                            return;
                        }
                    }

                }
            }
        }

        // if (players.interactionManager.getGameMode().equals(GameMode.SPECTATOR)) {

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
