package rainy.valo.Entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import rainy.valo.particles.ValoParticles;

import java.util.Optional;
import java.util.UUID;;

public class SoulOrbEntity extends Entity {
    public static final int LIFETIME = 300;
    private static final TrackedData<Optional<UUID>> OWNER =
            DataTracker.registerData(SoulOrbEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private int keepUntil;

    public SoulOrbEntity(EntityType<? extends SoulOrbEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public void setOwner(UUID owner) {
        this.dataTracker.set(OWNER, Optional.ofNullable(owner));
    }

    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER).orElse(null);
    }

    public void keepAlive() {
        keepUntil = age + 3;
    }

    public static SoulOrbEntity findLooked(World world, PlayerEntity p, double range) {
        Vec3d start = p.getEyePos();
        Vec3d dir = p.getRotationVec(1f);
        Vec3d end = start.add(dir.multiply(range));
        SoulOrbEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (SoulOrbEntity o : world.getEntitiesByClass(SoulOrbEntity.class,
                p.getBoundingBox().stretch(dir.multiply(range)).expand(1.5),
                o -> p.getUuid().equals(o.getOwnerUuid()))) {
            Optional<Vec3d> hit = o.getBoundingBox().expand(0.5).raycast(start, end);
            if (hit.isEmpty())
                continue;
            double d = start.squaredDistanceTo(hit.get());
            if (d >= bestD)
                continue;
            Vec3d c = o.getPos().add(0, 0.25, 0);
            if (world.raycast(new RaycastContext(start, c, RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, p)).getType() != HitResult.Type.MISS)
                continue;
            best = o;
            bestD = d;
        }
        return best;
    }

    @Override
    public void tick() {
        super.tick();
        World world = getWorld();
        if (world.isClient) {
            if (age % 3 == 0) {
                world.addParticle(ValoParticles.SOUL_WISP, getX() + (random.nextDouble() - 0.5) * 0.3, getY() + 0.3,
                        getZ() + (random.nextDouble() - 0.5) * 0.3, 0, 0.02, 0);
            }
        } else if (age >= LIFETIME && age >= keepUntil) {
            discard();
        }
    }

    @Override public boolean isAttackable() {
        return false;
    }
    @Override protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER, Optional.empty());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) setOwner(nbt.getUuid("Owner"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        UUID o = getOwnerUuid();
        if (o != null)
            nbt.putUuid("Owner", o);
    }
}