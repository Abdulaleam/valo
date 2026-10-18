package rainy.valo.Entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import rainy.valo.Valo;

public final class ModEntities {

    public static final EntityType<SoulOrbEntity> SOUL_ORB = register("soul_orb",
            EntityType.Builder.create(SoulOrbEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5f, 0.5f).maxTrackingRange(10).makeFireImmune());


    public static final EntityType<LeerEntity> LEER_EYE = register("leer_eye",
            EntityType.Builder.create(LeerEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 0.6f).maxTrackingRange(10).makeFireImmune());


    private static<T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, Identifier.of(Valo.MOD_ID, name), builder.build(name));
    }

    public static void registerEntities() {

    }
    private ModEntities() {

    }


}
