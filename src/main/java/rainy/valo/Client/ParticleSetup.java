package rainy.valo.Client;

import rainy.valo.particles.ValoParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.particle.SimpleParticleType;

/** Per-particle look: size (blocks), lifetime (ticks), gravity (negative = rises), glow. */
public final class ParticleSetup {
    private ParticleSetup() {}

    public static void init() {
        reg(ValoParticles.SOUL_WISP,     0.22f, 30, -0.02f, true);
        reg(ValoParticles.EYE_GLINT,     0.18f, 14,  0.00f, true);
        reg(ValoParticles.EMPRESS_AURA,  0.30f, 24, -0.03f, true);
        reg(ValoParticles.DISMISS_BURST, 0.55f, 18,  0.00f, true);
        reg(ValoParticles.DEVOUR_HEAL,   0.22f, 26, -0.04f, true);
    }

    private static void reg(SimpleParticleType type, float size, int life, float gravity, boolean glow) {
        ParticleFactoryRegistry.getInstance().register(type,
                sprites -> new ValoParticle.Factory(sprites, size, life, gravity, glow));
    }
}
