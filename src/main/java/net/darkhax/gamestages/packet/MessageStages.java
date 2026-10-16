package net.darkhax.gamestages.packet;

import net.darkhax.gamestages.GameStages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record MessageStages(List<String> stages) implements CustomPacketPayload {

    public static final Type<MessageStages> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GameStages.MOD_ID, "stages"));
    public static final StreamCodec<FriendlyByteBuf, MessageStages> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MessageStages::stages, MessageStages::new);

    public MessageStages(Collection<String> stages) {
        this(List.copyOf(new ArrayList<>(stages)));
    }

    public List<String> getStages() {
        return this.stages;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
