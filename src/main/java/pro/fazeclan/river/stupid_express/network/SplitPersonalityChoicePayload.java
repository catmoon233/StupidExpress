package pro.fazeclan.river.stupid_express.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import pro.fazeclan.river.stupid_express.StupidExpress;

public record SplitPersonalityChoicePayload(int choice) implements CustomPacketPayload {
    public static final Type<SplitPersonalityChoicePayload> ID = new Type<>(
        StupidExpress.id("split_personality_choice")
    );
    
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}