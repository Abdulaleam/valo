package rainy.valo;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AbilityPayLoad(int slot) implements CustomPayload {

    public static final Id<AbilityPayLoad> ID = new Id<>(Identifier.of(Valo.MOD_ID, "ability"));
    public static final PacketCodec<RegistryByteBuf, AbilityPayLoad> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, AbilityPayLoad::slot, AbilityPayLoad::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
