package rainy.valo.Client;

import com.mojang.blaze3d.systems.RenderSystem;
import rainy.valo.Valo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public final class ClientAssets {
    private static final int[] MISSING = new int[0];
    private static final Map<Identifier, int[]> SIZES = new HashMap<>();
    private static final Map<String, Identifier> IDS = new HashMap<>();

    private ClientAssets() {}

    public static Identifier gui(String path) {

        return IDS.computeIfAbsent(path,
                p -> Identifier.of(Valo.MOD_ID, "textures/gui/" + p + ".png"));
    }

    public static int[] size(Identifier id) {

        int[] cached = SIZES.get(id);
        if (cached == null) {
            cached = load(id);
            SIZES.put(id, cached);
        }
        return cached == MISSING ? null : cached;
    }

    private static int[] load(Identifier id) {
        Optional<Resource> res = MinecraftClient.getInstance().getResourceManager().getResource(id);
        if (res.isEmpty()) return MISSING;
        try (InputStream in = res.get().getInputStream(); NativeImage img = NativeImage.read(in)) {
            return new int[]{img.getWidth(), img.getHeight()};
        } catch (IOException e) {
            return MISSING;
        }
    }

    public static boolean has(Identifier id) {
        return size(id) != null;
    }

    public static void clearCache() {
        SIZES.clear();
    }

    public static boolean draw(DrawContext ctx, Identifier tex, int x, int y, int w, int h) {
        return draw(ctx, tex, x, y, w, h, 1f, 1f, 1f, 1f);
    }

    public static boolean draw(DrawContext ctx, Identifier tex, int x, int y, int w, int h,
                               float r, float g, float b, float a) {
        int[] s = size(tex);
        if (s == null)
            return false;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ctx.setShaderColor(r, g, b, a);
        ctx.drawTexture(tex, x, y, w, h, 0f, 0f, s[0], s[1], s[0], s[1]);
        ctx.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        return true;
    }

    public static boolean drawFit(DrawContext ctx, Identifier tex, int x, int y, int maxW, int maxH) {
        return drawFit(ctx, tex, x, y, maxW, maxH, 1f);
    }

    public static boolean drawFit(DrawContext ctx, Identifier tex, int x, int y, int maxW, int maxH, float alpha) {
        int[] s = size(tex);
        if (s == null)
            return false;
        float scale = Math.min(maxW / (float) s[0], maxH / (float) s[1]);
        int w = Math.round(s[0] * scale), h = Math.round(s[1] * scale);
        return draw(ctx, tex, x + (maxW - w) / 2, y + (maxH - h) / 2, w, h, 1f, 1f, 1f, alpha);
    }

    public static boolean drawFrame(DrawContext ctx, Identifier tex, int frame, int frames,
                                    int x, int y, int w, int h, float alpha) {
        int[] s = size(tex);
        if (s == null || frames <= 0) return false;
        int fw = s[0], fh = s[1] / frames;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ctx.setShaderColor(1f, 1f, 1f, alpha);
        ctx.drawTexture(tex, x, y, w, h, 0f, (float) (frame * fh), fw, fh, s[0], s[1]);
        ctx.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        return true;
    }
}
