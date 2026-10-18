package rainy.valo.Entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import rainy.valo.particles.ValoParticles;

import java.util.UUID;

public class LeerEntity extends Entity {

    public static final double SPEED = 1.0;
    public static final int TRAVEL_TICKS = 12;
    public static final int LIFETIME = 60;
    public static final double RADIUS = 10.0;

    private UUID owner;
    private boolean stopped;

    public LeerEntity(EntityType<? extends LeerEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        super.tick();
        World world = getWorld();

        if (world.isClient) {
            if (age % 2 == 0) {
                world.addParticle(ValoParticles.EYE_GLINT, getX(), getY() + 0.3, getZ(),
                        (random.nextDouble() - 0.5) * 0.5, random.nextDouble() * 0.3, (random.nextDouble() - 0.5) * 0.5);
            }
            return;
        }

        if (!stopped) {
            if (age < TRAVEL_TICKS) {
                move(MovementType.SELF, getVelocity());
                if (horizontalCollision || verticalCollision)
                    stop();
            } else {
                stop();
            }
        }

        if (age >= LIFETIME) {
            discard();
            return;
        }
        if (age % 4 == 0) {

            for (LivingEntity e: world.getEntitiesByClass(LivingEntity.class, getBoundingBox().expand(RADIUS), x ->
                    !x.getUuid().equals(owner))) {
                Vec3d toEye = getPos().subtract(e.getEyePos()).normalize();
                boolean facing = e.getRotationVec(1f).dotProduct(toEye) > 0.5;
                if (facing && e.canSee(this)) {
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0));
                }
            }

        }
    }

    private void stop() {

        setVelocity(Vec3d.ZERO);
        stopped = true;
        ((ServerWorld) getWorld()).spawnParticles(ValoParticles.EYE_GLINT, getX(), getY() + 0.3, getZ(), 12, 0.3, 0.3, 0.3, 0.05);
        playSound(SoundEvents.ENTITY_ENDERMAN_STARE, 0.6f, 1.8f);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean damage (DamageSource source, float amount) {
        if (getWorld().isClient || isRemoved())

            return false;
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker.getUuid().equals(owner))
            return false;

        ((ServerWorld) getWorld()).spawnParticles(ValoParticles.EYE_GLINT, getX(), getY() + 0.3, getZ(), 20, 0.2, 0.2, 0.2 , 0.1);
        playSound(SoundEvents.ENTITY_ENDERMAN_DEATH, 0.8f, 1.6f);
        discard();
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) owner = nbt.getUuid("Owner");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (owner != null)
            nbt.putUuid("Owner", owner);
    }
}