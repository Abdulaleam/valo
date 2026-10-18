package rainy.valo.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import rainy.valo.Valo;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValoParticles {

    private static final Map<String, SimpleParticleType> ALL = new LinkedHashMap<>();

    public static final SimpleParticleType SOUL_WISP = register("soul_wisp");
    public static final SimpleParticleType EYE_GLINT = register("eye_glint");
    public static final SimpleParticleType EMPRESS_AURA = register("empress_aura");
    public static final SimpleParticleType DISMISS_BURST = register("dismiss_burst");
    public static final SimpleParticleType DEVOUR_HEAL = register("devour_heal");





    private static SimpleParticleType register(String name) {
        SimpleParticleType type = Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Valo.MOD_ID, name),
                FabricParticleTypes.simple());
        ALL.put(name, type);
        return type;
    }

    public static Map<String, SimpleParticleType> all() {
        return ALL;
    }

    public static void registerValoParticles() {

    }
    private ValoParticles() {

    }
}
