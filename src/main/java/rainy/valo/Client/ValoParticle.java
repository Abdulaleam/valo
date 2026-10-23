package rainy.valo.Client;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class ValoParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;
    private final boolean glow;

    protected ValoParticle(ClientWorld world, double x, double y, double z,
                           double vx, double vy, double vz,
                           SpriteProvider sprites, float size, int life, float gravity, boolean glow) {
        super(world, x, y, z);
        this.sprites = sprites;
        this.glow = glow;
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.scale = size * (0.8f + random.nextFloat() * 0.4f);
        this.maxAge = life + random.nextInt(Math.max(1, life / 4));
        this.gravityStrength = gravity;
        this.collidesWithWorld = false;
        setSpriteForAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive()) return;
        setSpriteForAge(sprites);
        float t = age / (float) maxAge;
        this.alpha = t < 0.15f ? t / 0.15f : 1f - (t - 0.15f) / 0.85f; // fade in, then out
        velocityX *= 0.92;
        velocityY *= 0.92;
        velocityZ *= 0.92;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getBrightness(float tint) {
        return glow ? 0xF000F0 : super.getBrightness(tint);
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider sprites;
        private final float size;
        private final int life;
        private final float gravity;
        private final boolean glow;

        public Factory(SpriteProvider sprites, float size, int life, float gravity, boolean glow) {
            this.sprites = sprites;
            this.size = size;
            this.life = life;
            this.gravity = gravity;
            this.glow = glow;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new ValoParticle(world, x, y, z, vx, vy, vz, sprites, size, life, gravity, glow);
        }
    }
}
