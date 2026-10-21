package rainy.valo.Client;

import rainy.valo.network.AgentSyncPayload;

public final class ClientHudState {

    public static AgentSyncPayload data;
    private static long receivedMs;

    private ClientHudState() {}

    public static void update(AgentSyncPayload payload) {

        AgentSyncPayload prev = data;
        data = payload.agentId().isEmpty() ? null : payload;
        receivedMs = System.currentTimeMillis();

        if (data == null) {
            HudAnimation.stopAll();
        } else if (prev != null && prev.agentId().equals(data.agentId())
                && prev.ultPoints() < prev.ultCost() && data.ultPoints() >= data.ultCost()) {
            HudAnimation.ULT_READY.play();
        }
    }

    public static void clear() {
        data = null;
        HudAnimation.stopAll();
        ClientBanners.clear();
    }

    public static int elapsedTicks() {
        return (int) ((System.currentTimeMillis() - receivedMs) / 50L);
    }
}
