package rainy.valo.Client;

import java.util.HashMap;
import java.util.Map;


public final class HudAnimation {
    public static final SpriteAnimation ULT_READY =
            new SpriteAnimation(ClientAssets.gui("anim/ult_ready"), 16, 24, false);

    private static final Map<String, SpriteAnimation> ULT_ACTIVE = new HashMap<>();

    public static SpriteAnimation ultActive(String agentId) {
        return ULT_ACTIVE.computeIfAbsent(agentId,
                id -> new SpriteAnimation(ClientAssets.gui("anim/ult_active_" + id), 8, 12, true));
    }

    public static void stopAll() {
        ULT_READY.stop();
        ULT_ACTIVE.values().forEach(SpriteAnimation::stop);
    }

    private HudAnimation() {}
}
