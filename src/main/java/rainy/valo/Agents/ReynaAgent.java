package rainy.valo.Agents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import rainy.valo.Entity.LeerEntity;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.particles.ValoParticles;

public class ReynaAgent extends Agent {
    public static final ReynaAgent INSTANCE = new ReynaAgent();

    static final double ORB_RANGE = 10.0;
    static final int LEER_COOLDOWN = 400;
    static final int DISMISS_DURATION = 40;
    static final int EMPRESS_DURATION = 60000;

    private static final AbilityInfo[] ABILITIES = {
            new AbilityInfo("Leer", " Throw an Eye that nearsights anyone looking at it"),
            new AbilityInfo("Devour", "Consume a Soul Orb to heal and gain OverHeal"),
            new AbilityInfo("Dismiss", "Consume a soul orb to turn invisible and untouchable for 2s."),
            new AbilityInfo("Empress", "Gain speed, haste, strength , night vision"),
    };

    private static final class ReynaState extends AgentState {
        int dismissEnd;

        ReynaState(Agent agent) {
            super(agent);
        }
    }

    private ReynaAgent() {
    }

    @Override
    public String id() {
        return "reyna";
    }

    @Override
    public String displayName() {
        return "Reyna";
    }

    @Override
    public int color() {
        return 0xFFB04BFF;
    }

    @Override
    public int ultCost() {
        return 6;
    }

    @Override
    public AbilityInfo[] abilities() {
        return ABILITIES;
    }

    @Override
    public AgentState createState() {
        return new ReynaState(this);
    }

    @Override
    public boolean use(ServerPlayerEntity p, int slot, AgentState s, int now) {
        ReynaState st = (ReynaState) s;
        return switch (slot) {
            case 0 -> leer(p, st, now);
            case 1 -> devour(p, st, now);
            case 2 -> dismiss(p, st, now);
            case 3 -> empress(p, st, now);
            default -> false;
        };
    }

    @Override
    public void useActiveUlt(ServerPlayerEntity p, AgentState S, int now) {
        p.sendMessage(Text.literal("Empress is Already Active"), true);
    }

    private boolean leer(ServerPlayerEntity p, ReynaState s, int now) {
        ServerWorld world = p.getServerWorld();
        Vec3d look = p.getRotationVec(1f);

        LeerEntity eye = new LeerEntity(ModEntities.LEER_EYE, world);
        eye.setOwner(p.getUuid());
        eye.setPosition(p.getEyePos().add(look.multiply(0.6)).subtract(0, 0.3, 0));
        eye.setVelocity(look.multiply(LeerEntity.SPEED));
        world.spawnEntity(eye);

        s.startCooldown(0, LEER_COOLDOWN, now);
        world.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.PLAYERS, 1f, 1.5f);

        return true;
    }

    private boolean devour(ServerPlayerEntity p, ReynaState s, int now) {
        if (!consumeOrb(p, s, now)) return false;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 4));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 1));
        p.getServerWorld().spawnParticles(ValoParticles.DEVOUR_HEAL, p.getX(), p.getY() + 0.2, p.getZ(), 8, 0.4, 0.1, 0.4, 0.04);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_WITCH_DRINK, SoundCategory.PLAYERS, 1f, 0.8f);
        return true;
    }

    private boolean dismiss(ServerPlayerEntity p, ReynaState s, int now) {
        if (!consumeOrb(p, s, now)) return false;
        s.dismissEnd = now + DISMISS_DURATION;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, DISMISS_DURATION, 0, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DISMISS_DURATION, 1));
        p.getServerWorld().spawnParticles(ValoParticles.DISMISS_BURST, p.getX(), p.getY() + 1, p.getZ(), 8, 0.4, 0.8, 0.4, 0.05);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.2f);

        return true;
    }

    private boolean empress(ServerPlayerEntity p, ReynaState s, int now) {
        s.cooldownEnd[0] = now;
        applyEmpress(p, s, now);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    private void applyEmpress(ServerPlayerEntity p, ReynaState s, int now) {
        s.activeEnd = now + EMPRESS_DURATION;
        s.activeTotal = EMPRESS_DURATION;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EMPRESS_DURATION, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, EMPRESS_DURATION, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, EMPRESS_DURATION, 0));
    }


    private SoulOrbEntity nearestOrb(ServerPlayerEntity p) {
        SoulOrbEntity best = null;
        double bestDist = ORB_RANGE * ORB_RANGE;
        for (SoulOrbEntity o : p.getServerWorld().getEntitiesByClass(SoulOrbEntity.class,
                p.getBoundingBox().expand(ORB_RANGE), o -> p.getUuid().equals(o.getOwnerUuid()))) {
            double d = o.squaredDistanceTo(p);
            if (d <= bestDist) {
                best = o;
                bestDist = d;
            }
        }

        return best;
        }

        private boolean consumeOrb(ServerPlayerEntity p, ReynaState s, int now) {
            SoulOrbEntity orb = nearestOrb(p);
            if (orb != null) {
                ServerWorld world = p.getServerWorld();
                Vec3d from = orb.getPos().add(0, 0.3, 0);
                Vec3d to = p.getPos().add(0, 1, 0);
                for (int i = 0; i <= 10; i++) {
                    Vec3d v = from.lerp(to, 1 / 10.0);
                    world.spawnParticles(ValoParticles.SOUL_WISP, v.x, v.y, v.z, 1, 0.05, 0.05, 0.05, 0.0);

                }

                orb.discard();
                return true;
        }

            if (s.isUltActive(now))
                return true;
            p.sendMessage(Text.literal("No Soul orb Nearby"), true);
            return false;
    }
    @Override
    public void onKill(ServerPlayerEntity killer, LivingEntity victim, AgentState s, int now) {
        ServerWorld world = killer.getServerWorld();
        SoulOrbEntity orb = new SoulOrbEntity(ModEntities.SOUL_ORB, world);
        orb.setOwner(killer.getUuid());
        orb.setPosition(victim.getPos().add(0, 0.5, 0));
        world.spawnEntity(orb);

        if (s.isUltActive(now))
            applyEmpress(killer, (ReynaState) s, now);
    }

    @Override
    public void onTick(ServerPlayerEntity p, AgentState s, int now) {
        if (s.isUltActive(now) && now % 3 == 0) {
            p.getServerWorld().spawnParticles(ValoParticles.EMPRESS_AURA, p.getX(), p.getY() + 1.0, p.getZ(), 2,
                    0.4, 0.7, 0.4, 0.02);
        }
    }
    @Override
    public boolean allowDamage(ServerPlayerEntity p, DamageSource source, AgentState s, int now) {
        return ((ReynaState) s).dismissEnd <= now;
    }

    @Override
    public SlotView slotView(ServerPlayerEntity p, int slot, AgentState s, int now) {
        if (slot ==1 || slot == 2 ) {
            int orbs = p.getServerWorld().getEntitiesByClass(SoulOrbEntity.class,
                    p.getBoundingBox().expand(ORB_RANGE), o -> p.getUuid().equals(o.getOwnerUuid())).size();
            return new SlotView(0 ,0, orbs);
        }

        return super.slotView(p, slot, s, now);
    }


    @Override
    public String status(ServerPlayerEntity p, AgentState s, int now) {
        return s.isUltActive(now) ? "EMPRESS" + ((s.activeRemaining(now) + 19) / 20) + "s" : "";
    }


}
