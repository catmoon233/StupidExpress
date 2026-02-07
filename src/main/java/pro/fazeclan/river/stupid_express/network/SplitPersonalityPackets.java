package pro.fazeclan.river.stupid_express.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

public class SplitPersonalityPackets {
    
    public static final ResourceLocation CHOICE_PACKET_ID = StupidExpress.id("split_personality_choice");

    public static class SplitPersonalityChoicePayload implements CustomPacketPayload {
        public static final Type<SplitPersonalityChoicePayload> ID =new Type<>(CHOICE_PACKET_ID);

        public static final StreamCodec<FriendlyByteBuf, SplitPersonalityChoicePayload> CODEC =
                StreamCodec.ofMember(
                        SplitPersonalityChoicePayload::write,
                        SplitPersonalityChoicePayload::read

                );
        public void write(FriendlyByteBuf buf) {
            buf.writeInt(choice);
        }

        public static SplitPersonalityChoicePayload read(FriendlyByteBuf buf) {
            return new SplitPersonalityChoicePayload(buf.readInt());
        }
        private final int choice; // 0 = SACRIFICE, 1 = BETRAY

        public SplitPersonalityChoicePayload(int choice) {
            this.choice = choice;
        }


        public int getChoice() {
            return choice;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(SplitPersonalityChoicePayload.ID, SplitPersonalityChoicePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SplitPersonalityChoicePayload.ID, (payload, context) -> {
            context.server().submit(() -> {
                ServerPlayer player = context.player();
                var component = SplitPersonalityComponent.KEY.get(player);

                if (component == null || component.getMainPersonality() == null) {
                    return;
                }

                SplitPersonalityComponent.ChoiceType choice;
                if (payload.getChoice() == 0) {
                    choice = SplitPersonalityComponent.ChoiceType.SACRIFICE;
                } else {
                    choice = SplitPersonalityComponent.ChoiceType.BETRAY;
                }

                // 设置当前玩家的选择
                if (component.isMainPersonality()) {
                    component.setMainPersonalityChoice(choice);
                } else {
                    component.setSecondPersonalityChoice(choice);
                }
                component.sync();
                
                // 检查是否两个人都已选择，如果是则处理结果
                if (component.bothMadeChoice()) {
                    SplitPersonalityHandler.handleDeathChoices(player, component);
                }
            });
        });
    }

    public static void sendChoicePacket(int choice) {
        // 这个方法将在客户端代码中使用，所以需要特殊处理
        // 参见 SplitPersonalityKeybindsClient
    }
}
