package rainy.valo.Client;

import rainy.valo.Valo;
import rainy.valo.ValoClient;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.Agents.AbilityInfo;
import rainy.valo.Agents.Agent;
import rainy.valo.Agents.AgentRegistry;
import rainy.valo.network.AgentSyncPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class ValorantHud {
    private static final int BOX = 32;
    private static final int GAP = 5;
    private static final String[] SLOT = {"c", "q", "e", "x"};

    private ValorantHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        AgentSyncPayload d = ClientHudState.data;
        if (d == null) return;
        Agent agent = AgentRegistry.get(d.agentId());
        MinecraftClient mc = MinecraftClient.getInstance();
        if (agent == null || mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;
        int elapsed = ClientHudState.elapsedTicks();
        int accent = agent.color();
        float ar = ((accent >> 16) & 255) / 255f, ag = ((accent >> 8) & 255) / 255f, ab = (accent & 255) / 255f;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int x0 = 10;
        int y0 = sh - 72;
        int ax = x0 + 64;
        int totalW = BOX * 4 + GAP * 3;
        int activeRem = Math.max(0, d.activeRemaining() - elapsed);

        var aura = HudAnimation.ultActive(d.agentId());
        if (activeRem > 0) {
            aura.playIfStopped();
            aura.drawCover(ctx, sw, sh, 0.5f);
        } else {
            aura.stop();
        }

        ClientAssets.draw(ctx, ClientAssets.gui("crosshair/" + d.agentId()), sw / 2 - 20, sh / 2 - 20, 40, 40, 1f, 1f, 1f, 0.85f);
        ClientBanners.render(ctx, tr, sw, sh, accent);

        if (ClientHudState.leerArmed) {
            Identifier eye = Identifier.of(Valo.MOD_ID, "textures/entity/leer_eye.png");
            float t = (System.currentTimeMillis() % 2000L) / 2000f * 6.2831855f;
            int size = 96;
            int ex = sw - size - 90 + (int) (Math.sin(t) * 3);
            int ey = sh - size - 90 + (int) (Math.cos(t * 2) * 3);
            ClientAssets.draw(ctx, eye, ex, ey, size, size);
            ctx.drawCenteredTextWithShadow(tr, "LEFT CLICK to throw  |  " + ValoClient.keyLabel(0) + " to cancel",
                    sw / 2, sh - 110, 0xFFFFFFFF);
        }

        if (d.agentId().equals("reyna") && mc.world != null) {
            SoulOrbEntity looked = SoulOrbEntity.findLooked(mc.world, mc.player, 30.0);
            if (looked != null) {
                int cx = sw / 2, cy = sh / 2 + 28;
                float left = 1f - Math.min(1f, looked.age / (float) SoulOrbEntity.LIFETIME);
                ctx.fill(cx - 70, cy - 4, cx + 70, cy + 22, 0xAA101018);
                ctx.drawCenteredTextWithShadow(tr, "[" + ValoClient.keyLabel(1) + "] DEVOUR     [" + ValoClient.keyLabel(2) + "] DISMISS",
                        cx, cy, accent);
                ctx.fill(cx - 66, cy + 13, cx + 66, cy + 17, 0x88000000);
                ctx.fill(cx - 66, cy + 13, cx - 66 + (int) (132 * left), cy + 17, accent);
            }
        }

        int hp = Math.round(mc.player.getHealth() / mc.player.getMaxHealth() * 100f);
        int hcol = hp > 30 ? 0xFFFFFFFF : 0xFFFF4D4D;
        ctx.fill(x0 - 4, y0 - 2, x0 + 56, y0 + BOX + 14, 0xAA101018);
        ctx.getMatrices().push();
        ctx.getMatrices().scale(2.4f, 2.4f, 1f);
        ctx.drawTextWithShadow(tr, String.valueOf(hp), (int) ((x0 + 2) / 2.4f), (int) ((y0 + 2) / 2.4f), hcol);
        ctx.getMatrices().pop();
        ctx.fill(x0, y0 + 26, x0 + 52, y0 + 29, 0x88000000);
        ctx.fill(x0, y0 + 26, x0 + (int) (52 * hp / 100f), y0 + 29, hcol);
        smallCentered(ctx, tr, "HP", x0 + 26, y0 + 34, 0xFFAAAAAA);

        String name = agent.displayName().toUpperCase();
        int nameX = x0;
        if (ClientAssets.draw(ctx, ClientAssets.gui("agent/" + d.agentId() + "_icon"), x0, y0 - 26, 22, 22)) {
            nameX = x0 + 28;
        }
        ctx.drawTextWithShadow(tr, name, nameX, y0 - 14, accent);
        if (!d.status().isEmpty()) {
            ctx.drawTextWithShadow(tr, d.status(), nameX + tr.getWidth(name) + 8, y0 - 14, 0xFFE8E8E8);
        }

        if (activeRem > 0 && d.activeTotal() > 0) {
            int w = (int) (totalW * (activeRem / (float) d.activeTotal()));
            ctx.fill(ax, y0 - 4, ax + totalW, y0 - 1, 0x88000000);
            ctx.fill(ax, y0 - 4, ax + w, y0 - 1, accent);
        }

        HudAnimation.ULT_READY.draw(ctx, ax + totalW / 2 - 64, y0 - 150, 128, 128, 1f);

        AbilityInfo[] abilities = agent.abilities();
        Identifier frameTex = ClientAssets.gui("hud/slot_frame");
        for (int i = 0; i < 4; i++) {
            int x = ax + i * (BOX + GAP);
            int y = y0;
            int rem = Math.max(0, d.cdRemaining()[i] - elapsed);
            int total = d.cdTotal()[i];
            int charges = d.charges()[i];
            boolean ultSlot = i == 3;
            boolean usable = rem == 0 && charges != 0 && charges != -2
                    && (!ultSlot || d.ultPoints() >= d.ultCost() || activeRem > 0);

            ctx.fill(x, y, x + BOX, y + BOX, 0xCC101018);

            Identifier icon = ClientAssets.gui("ability/" + d.agentId() + "_" + SLOT[i]);
            float t = usable ? 1f : 0.35f;
            boolean hasIcon = ClientAssets.draw(ctx, icon, x + 2, y + 2, BOX - 4, BOX - 4, t, t, t, 1f);
            if (!hasIcon) {
                smallCentered(ctx, tr, abilities[i].name(), x + BOX / 2, y + BOX / 2 - 3, usable ? 0xFFFFFFFF : 0xFF777788);
            }

            if (rem > 0 && total > 0) {
                int hh = (int) (BOX * Math.min(1f, rem / (float) total));
                ctx.fill(x, y, x + BOX, y + hh, 0xAA000000);
                ctx.drawCenteredTextWithShadow(tr, String.valueOf((rem + 19) / 20), x + BOX / 2, y + BOX / 2 - 4, 0xFFFFFFFF);
            }

            float fr = usable ? ar : 0.35f, fg = usable ? ag : 0.35f, fb = usable ? ab : 0.4f;
            if (!ClientAssets.draw(ctx, frameTex, x, y, BOX, BOX, fr, fg, fb, 1f)) {
                ctx.drawBorder(x, y, BOX, BOX, usable ? accent : 0xFF555566);
            }

            String key = ValoClient.keyLabel(i).toUpperCase();
            int kw = tr.getWidth(key);
            int kx = x + BOX / 2 - kw / 2;
            ctx.fill(kx - 3, y + BOX + 2, kx + kw + 3, y + BOX + 13, 0xCC101018);
            ctx.fill(kx - 3, y + BOX + 12, kx + kw + 3, y + BOX + 13, usable ? accent : 0xFF555566);
            ctx.drawTextWithShadow(tr, key, kx, y + BOX + 3, usable ? 0xFFFFFFFF : 0xFF888899);

            if (charges >= 0) {
                for (int n = 0; n < charges; n++) {
                    int dx = x + 4 + n * 7;
                    ctx.fill(dx, y + BOX - 7, dx + 5, y + BOX - 2, accent);
                }
            }
        }

        int pipY = y0 + BOX + 18;
        boolean ready = d.ultPoints() >= d.ultCost();
        Identifier pipFull = ClientAssets.gui("hud/pip_full");
        Identifier pipEmpty = ClientAssets.gui("hud/pip_empty");
        for (int k = 0; k < d.ultCost(); k++) {
            int px = ax + k * 10;
            boolean filled = k < d.ultPoints();
            boolean drawn;
            if (filled) {
                float r = ready ? 1f : ar, g = ready ? 0.83f : ag, b = ready ? 0.3f : ab;
                drawn = ClientAssets.draw(ctx, pipFull, px, pipY, 9, 9, r, g, b, 1f);
            } else {
                drawn = ClientAssets.draw(ctx, pipEmpty, px, pipY, 9, 9);
            }
            if (!drawn) {
                ctx.fill(px, pipY, px + 7, pipY + 7, filled ? (ready ? 0xFFFFD34D : accent) : 0x88333344);
            }
        }
        if (ready && activeRem == 0) {
            ctx.drawTextWithShadow(tr, "READY [" + ValoClient.keyLabel(3) + "]",
                    ax + d.ultCost() * 10 + 4, pipY, 0xFFFFD34D);
        }
    }

    private static void smallCentered(DrawContext ctx, TextRenderer tr, String text, int cx, int y, int color) {
        float s = 0.6f;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(s, s, 1f);
        ctx.drawCenteredTextWithShadow(tr, text, (int) (cx / s), (int) (y / s), color);
        ctx.getMatrices().pop();
    }
}