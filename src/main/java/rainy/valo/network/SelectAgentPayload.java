package rainy.valo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import rainy.valo.Valo;

public record SelectAgentPayload(String id) implements CustomPayload {
    public static final Id<SelectAgentPayload> ID = new Id<>(Identifier.of(Valo.MOD_ID, "select_agent"));
    public static final PacketCodec<RegistryByteBuf, SelectAgentPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, SelectAgentPayload::id, SelectAgentPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
