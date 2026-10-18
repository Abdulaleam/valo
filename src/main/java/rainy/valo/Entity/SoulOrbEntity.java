package rainy.valo.Entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import rainy.valo.particles.ValoParticles;

import java.util.UUID;

public class SoulOrbEntity extends Entity {
    public static final int LIFETIME = 100;
    private UUID owner;

    public SoulOrbEntity(EntityType<? extends SoulOrbEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwnerUuid() {
        return owner;
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

        } else if (age >= LIFETIME) {
            discard();
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected  void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) owner = nbt.getUuid("Owner");
    }
    @Override
    protected  void writeCustomDataToNbt(NbtCompound nbt) {
        if (owner != null)
            nbt.putUuid("Owner", owner);
    }


}
