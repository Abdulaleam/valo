package rainy.valo.Client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class ClientBanners {
    private static final long KILL_MS = 1800;
    private static final long HIT_MS = 350;

    private static int streak;
    private static long killStart = -1;
    private static long hitStart = -1;

    private ClientBanners() {}

    public static void kill(int s) {
        streak = s;
        killStart = System.currentTimeMillis();
    }

    public static void hit() {
        hitStart = System.currentTimeMillis();
    }

    public static void clear() {
        killStart = -1;
        hitStart = -1;
    }

    static String bannerName(int s) {
        return switch (Math.min(Math.max(s, 1), 5)) {
            case 1 -> "kill";
            case 2 -> "double";
            case 3 -> "triple";
            case 4 -> "quad";
            default -> "ace";
        };
    }

    private static String fallbackText(int s) {
        return switch (Math.min(Math.max(s, 1), 5)) {
            case 1 -> "ELIMINATED";
            case 2 -> "DOUBLE KILL";
            case 3 -> "TRIPLE KILL";
            case 4 -> "QUADRA KILL";
            default -> "ACE";
        };
    }

    public static void render(DrawContext ctx, TextRenderer tr, int sw, int sh, int accent) {
        long now = System.currentTimeMillis();

        if (killStart >= 0) {
            long el = now - killStart;
            if (el > KILL_MS) {
                killStart = -1;
            } else {
                float fadeIn = Math.min(1f, el / 150f);
                float fadeOut = Math.min(1f, (KILL_MS - el) / 400f);
                float alpha = Math.min(fadeIn, fadeOut);
                int slide = (int) ((1f - fadeIn) * -16);
                int bw = 320, bh = 80;
                int bx = sw / 2 - bw / 2, by = 36 + slide;
                Identifier tex = ClientAssets.gui("banner/" + bannerName(streak));
                if (!ClientAssets.drawFit(ctx, tex, bx, by, bw, bh, alpha)) {
                    int a = Math.max(8, (int) (alpha * 255));
                    int color = (a << 24) | (accent & 0xFFFFFF);
                    ctx.getMatrices().push();
                    ctx.getMatrices().scale(2f, 2f, 1f);
                    ctx.drawCenteredTextWithShadow(tr, fallbackText(streak), sw / 4, (by + 30) / 2, color);
                    ctx.getMatrices().pop();
                }
            }
        }

        if (hitStart >= 0) {
            long el = now - hitStart;
            if (el > HIT_MS) {
                hitStart = -1;
            } else {
                float alpha = 1f - el / (float) HIT_MS;
                Identifier tex = ClientAssets.gui("hud/hitmarker");
                if (!ClientAssets.draw(ctx, tex, sw / 2 - 16, sh / 2 - 16, 32, 32, 1f, 1f, 1f, alpha)) {
                    int c = (Math.max(8, (int) (alpha * 255)) << 24) | 0xFFFFFF;
                    for (int i = 4; i < 10; i++) {
                        int cx = sw / 2, cy = sh / 2;
                        ctx.fill(cx + i, cy + i, cx + i + 2, cy + i + 2, c);
                        ctx.fill(cx - i - 2, cy + i, cx - i, cy + i + 2, c);
                        ctx.fill(cx + i, cy - i - 2, cx + i + 2, cy - i, c);
                        ctx.fill(cx - i - 2, cy - i - 2, cx - i, cy - i, c);
                    }
                }
            }
        }
    }
}
