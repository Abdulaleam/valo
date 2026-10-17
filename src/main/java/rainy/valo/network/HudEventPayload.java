package rainy.valo.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import rainy.valo.Valo;

public record HudEventPayload(int type, int value) implements CustomPayload {
    public static final Id<HudEventPayload> ID = new Id<>(Identifier.of(Valo.MOD_ID, "hud_events"));
    public static final PacketCodec<RegistryByteBuf, HudEventPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, HudEventPayload::type,PacketCodecs.VAR_INT, HudEventPayload::value
                    , HudEventPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
