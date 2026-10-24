package rainy.valo.Client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class SpriteAnimation {
    private final Identifier texture;
    private final int frames;
    private final int fps;
    private final boolean loop;
    private long startMs = -1;


    public SpriteAnimation(Identifier texture, int frames, int fps, boolean loop) {
        this.texture = texture;
        this.frames = frames;
        this.fps = fps;
        this.loop = loop;
    }

    public void play() {
        startMs = System.currentTimeMillis();
    }

    public void playIfStopped() {
        if (!isPlaying())
            play();
    }
    public void stop() {
        startMs = -1;
    }
    private int frameIndex() {
        return (int) ((System.currentTimeMillis() - startMs) * fps / 1000L);
    }

    public boolean isPlaying() {
        if (startMs < 0)
            return false;
        return loop || frameIndex() < frames;
    }

    public void drawCover(DrawContext ctx, int sw, int sh, float alpha) {
        if (!isPlaying())
            return;
        int f = frameIndex();
        if (loop) f %= frames;
        if (f >= frames) { stop();
            return;
        }
        int[] s = ClientAssets.size(texture);
        if (s == null)
            return;
        float fw = s[0], fh = (float) s[1] / frames;
        float scale = Math.max(sw / fw, sh / fh);
        int w = Math.round(fw * scale), h = Math.round(fh * scale);
        ClientAssets.drawFrame(ctx, texture, f, frames, (sw - w) / 2, (sh - h) / 2, w, h, alpha);
    }

    public void draw(DrawContext ctx, int x, int y, int w, int h, float alpha) {
        if (!isPlaying()) return;
        int f = frameIndex();
        if (loop) f %= frames;
        if (f >= frames) { stop(); return; }
        ClientAssets.drawFrame(ctx, texture, f, frames, x, y, w, h, alpha);
    }
}
