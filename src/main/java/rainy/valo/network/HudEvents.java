package rainy.valo.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class HudEvents {
    public static final int KILL = 0;
    public static final int HIT = 1;

    private HudEvents() {}

    public static void send(ServerPlayerEntity p, int type, int value) {
        ServerPlayNetworking.send(p, new HudEventPayload(type, value));
    }
}
