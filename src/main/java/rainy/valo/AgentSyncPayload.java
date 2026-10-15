package rainy.valo;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AgentSyncPayload(String agentId, int ultPoints, int ultCost, int activeRemaining, int activeTotal,
                               String status, int[] cdRemaining, int[] cdTotal, int [] charges) implements CustomPayload {
    public static final Id<AgentSyncPayload> ID = new Id<>(Identifier.of(Valo.MOD_ID, "agent_sync"));
    public static final PacketCodec<RegistryByteBuf, AgentSyncPayload> CODEC =
            PacketCodec.of(AgentSyncPayload::write, AgentSyncPayload::read);

    public static AgentSyncPayload empty() {
        return new AgentSyncPayload("", 0 , 0, 0,0,"", new int[4], new int[4], new int[4]);
    }
    private void write(RegistryByteBuf buf) {
        buf.writeString(agentId);
        buf.writeVarInt(ultPoints);
        buf.writeVarInt(ultCost);
        buf.writeVarInt(activeRemaining);
        buf.writeVarInt(activeTotal);
        buf.writeString(status);

        for (int i = 0; i < 4; i++) {
            buf.writeVarInt(cdRemaining[i]);
            buf.writeVarInt(cdTotal[i]);
            buf.writeVarInt(charges[i]);

        }

    }
    private static AgentSyncPayload read(RegistryByteBuf buf) {
        String agentId = buf.readString();
        int ultPoints = buf.readVarInt();
        int ultCost = buf.readVarInt();
        int activeRemaining = buf.readVarInt();
        int activeTotal = buf.readVarInt();
        String status = buf.readString();
        int [] cdR = new int[4], cdT = new int[4], ch = new int[4];

        for (int i =0; i < 4; i++) {
            cdR[i] = buf.readVarInt();
            cdT[i] = buf.readVarInt();
        }
        return new AgentSyncPayload(agentId, ultPoints, ultCost, activeRemaining, activeTotal, status, cdR, cdT,ch);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
