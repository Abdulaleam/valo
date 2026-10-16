package rainy.valo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

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
                world.addParticle(ParticleTypes.PORTAL, getX(), getY() + 0.3, getZ(),
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
            
        }
    }
}
