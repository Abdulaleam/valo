package rainy.valo.Agents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import rainy.valo.Entity.LeerEntity;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.HudEvents;
import rainy.valo.particles.ValoParticles;

import java.util.UUID;

public class ReynaAgent extends Agent {
    public static final ReynaAgent INSTANCE = new ReynaAgent();

    static final double ORB_RANGE = 30.0;
    static final double DEVOUR_RANGE = 24.0;
    static final int LEER_CHARGES = 2;
    static final int LEER_DELAY = 20;
    static final int ACTION_DELAY = 10;
    static final int DEVOUR_TICKS = 60;
    static final int DISMISS_DURATION = 40;
    static final int EMPRESS_DURATION = 600;

    private static final AbilityInfo[] ABILITIES = {
            new AbilityInfo("Leer", "2 charges. Equip the eye, then left click to throw it where you aim. Nearsights anyone looking at it. Destructible."),
            new AbilityInfo("Devour", "Look at a soul orb and cast. A purple line heals you while it has a clear path to the orb."),
            new AbilityInfo("Dismiss", "Look at a soul orb and cast. Become invisible and untouchable for 2s."),
            new AbilityInfo("Empress", "30s of speed, haste, strength and night vision. Kills renew it. Devour and Dismiss need no orb.")
    };

    private static final class ReynaState extends AgentState {
        int dismissEnd;
        boolean leerArmed;
        int leerCharges = LEER_CHARGES;
        UUID devourOrb;
        int devourLeft;

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
        if (slot != 0) disarm(p, st);
        return switch (slot) {
            case 0 -> leer(p, st);
            case 1 -> devour(p, st, now);
            case 2 -> dismiss(p, st, now);
            case 3 -> empress(p, st, now);
            default -> false;
        };
    }

    @Override
    public void useActiveUlt(ServerPlayerEntity p, AgentState s, int now) {
        p.sendMessage(Text.literal("Empress is Already Active"), true);
    }

    private void disarm(ServerPlayerEntity p, ReynaState s) {
        if (!s.leerArmed) return;
        s.leerArmed = false;
        HudEvents.send(p, HudEvents.ARM, 0);
    }

    private boolean leer(ServerPlayerEntity p, ReynaState s) {
        if (s.leerArmed) {
            disarm(p, s);
            return false;
        }
        if (s.leerCharges <= 0) {
            p.sendMessage(Text.literal("No Leer charges"), true);
            return false;
        }
        s.leerArmed = true;
        HudEvents.send(p, HudEvents.ARM, 1);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.PLAYERS, 0.5f, 1.2f);
        return false;
    }

    @Override
    public boolean onPrimaryFire(ServerPlayerEntity p, AgentState s, int now) {
        ReynaState st = (ReynaState) s;
        if (!st.leerArmed || st.leerCharges <= 0) return false;

        ServerWorld world = p.getServerWorld();
        Vec3d look = p.getRotationVec(1f);

        LeerEntity eye = new LeerEntity(ModEntities.LEER_EYE, world);
        eye.setOwner(p.getUuid());
        eye.setPosition(p.getEyePos().add(look.multiply(0.6)).subtract(0, 0.3, 0));
        eye.setVelocity(look.multiply(LeerEntity.SPEED));
        world.spawnEntity(eye);

        st.leerCharges--;
        st.startCooldown(0, LEER_DELAY, now);
        disarm(p, st);
        world.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.PLAYERS, 1f, 1.5f);
        return true;
    }

    private boolean devour(ServerPlayerEntity p, ReynaState s, int now) {
        ServerWorld world = p.getServerWorld();
        if (s.devourOrb != null) {
            p.sendMessage(Text.literal("Devour is already active"), true);
            return false;
        }
        SoulOrbEntity orb = SoulOrbEntity.findLooked(world, p, ORB_RANGE);
        if (orb == null && !s.isUltActive(now)) {
            p.sendMessage(Text.literal("Look at a soul orb"), true);
            return false;
        }
        if (orb != null) {
            s.devourOrb = orb.getUuid();
            s.devourLeft = DEVOUR_TICKS;
            orb.keepAlive();
        } else {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 4));
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, 0));
        world.spawnParticles(ValoParticles.DEVOUR_HEAL, p.getX(), p.getY() + 0.2, p.getZ(), 6, 0.4, 0.1, 0.4, 0.04);
        world.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_WITCH_DRINK, SoundCategory.PLAYERS, 1f, 0.8f);
        s.startCooldown(1, ACTION_DELAY, now);
        return true;
    }

    private boolean dismiss(ServerPlayerEntity p, ReynaState s, int now) {
        ServerWorld world = p.getServerWorld();
        SoulOrbEntity orb = SoulOrbEntity.findLooked(world, p, ORB_RANGE);
        if (orb == null && !s.isUltActive(now)) {
            p.sendMessage(Text.literal("Look at a soul orb"), true);
            return false;
        }
        if (orb != null) {
            if (orb.getUuid().equals(s.devourOrb)) {
                p.sendMessage(Text.literal("That orb is being devoured"), true);
                return false;
            }
            absorb(p, orb);
        }
        s.dismissEnd = now + DISMISS_DURATION;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, DISMISS_DURATION, 0, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DISMISS_DURATION, 3));
        world.spawnParticles(ValoParticles.DISMISS_BURST, p.getX(), p.getY() + 1, p.getZ(), 8, 0.4, 0.8, 0.4, 0.05);
        world.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.2f);
        s.startCooldown(2, ACTION_DELAY, now);
        return true;
    }

    private boolean empress(ServerPlayerEntity p, ReynaState s, int now) {
        applyEmpress(p, s, now);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.6f, 1.6f);
        return true;
    }

    private void applyEmpress(ServerPlayerEntity p, ReynaState s, int now) {
        s.activeEnd = now + EMPRESS_DURATION;
        s.activeTotal = EMPRESS_DURATION;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EMPRESS_DURATION, 1));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, EMPRESS_DURATION, 2));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, EMPRESS_DURATION, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, EMPRESS_DURATION, 0));
    }

    private void absorb(ServerPlayerEntity p, SoulOrbEntity orb) {
        ServerWorld world = p.getServerWorld();
        Vec3d from = orb.getPos().add(0, 0.3, 0);
        Vec3d to = p.getPos().add(0, 1, 0);
        for (int i = 0; i <= 10; i++) {
            Vec3d v = from.lerp(to, i / 10.0);
            world.spawnParticles(ValoParticles.SOUL_WISP, v.x, v.y, v.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        orb.discard();
    }

    private boolean hasOrb(ServerPlayerEntity p) {
        return !p.getServerWorld().getEntitiesByClass(SoulOrbEntity.class,
                p.getBoundingBox().expand(ORB_RANGE), o -> p.getUuid().equals(o.getOwnerUuid())).isEmpty();
    }

    private void tickDevour(ServerPlayerEntity p, ReynaState s) {
        if (s.devourOrb == null) return;
        ServerWorld world = p.getServerWorld();
        Entity e = world.getEntity(s.devourOrb);
        if (!(e instanceof SoulOrbEntity orb) || p.isDead()) {
            s.devourOrb = null;
            return;
        }
        orb.keepAlive();

        Vec3d from = p.getPos().add(0, 1.0, 0);
        Vec3d to = orb.getPos().add(0, 0.3, 0);
        double len = from.distanceTo(to);
        boolean clear = len <= DEVOUR_RANGE && world.raycast(new RaycastContext(from, to,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p)).getType() == HitResult.Type.MISS;
        if (!clear) return;

        int t = p.getServer().getTicks();
        if (t % 3 == 0) p.heal(1f);
        if (t % 2 == 0) {
            Vec3d dir = to.subtract(from).normalize();
            for (double d = 0; d < len; d += 0.5) {
                Vec3d v = from.add(dir.multiply(d));
                world.spawnParticles(ValoParticles.SOUL_WISP, v.x, v.y, v.z, 1, 0, 0, 0, 0);
            }
        }
        s.devourLeft--;
        if (s.devourLeft <= 0) {
            orb.discard();
            s.devourOrb = null;
        }
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
        ReynaState st = (ReynaState) s;
        if (st.leerArmed && (p.isDead() || p.isSpectator())) disarm(p, st);
        tickDevour(p, st);
        if (s.isUltActive(now) && now % 3 == 0) {
            p.getServerWorld().spawnParticles(ValoParticles.EMPRESS_AURA, p.getX(), p.getY() + 1.0, p.getZ(), 2,
                    0.4, 0.7, 0.4, 0.02);
        }
    }

    @Override
    public void onDeselect(ServerPlayerEntity p, AgentState s) {
        ((ReynaState) s).devourOrb = null;
        HudEvents.send(p, HudEvents.ARM, 0);
    }

    @Override
    public boolean allowDamage(ServerPlayerEntity p, DamageSource source, AgentState s, int now) {
        return ((ReynaState) s).dismissEnd <= now;
    }

    @Override
    public SlotView slotView(ServerPlayerEntity p, int slot, AgentState s, int now) {
        ReynaState st = (ReynaState) s;
        if (slot == 0) {
            return new SlotView(s.cooldownRemaining(0, now), s.cooldownTotal[0], st.leerCharges);
        }
        if (slot == 1 || slot == 2) {
            boolean ok = s.isUltActive(now) || hasOrb(p);
            return new SlotView(s.cooldownRemaining(slot, now), s.cooldownTotal[slot], ok ? -1 : -2);
        }
        return super.slotView(p, slot, s, now);
    }

    @Override
    public String status(ServerPlayerEntity p, AgentState s, int now) {
        ReynaState st = (ReynaState) s;
        if (st.leerArmed) return "LEER: LEFT CLICK TO THROW";
        if (st.devourOrb != null) return "DEVOURING";
        return s.isUltActive(now) ? "EMPRESS " + ((s.activeRemaining(now) + 19) / 20) + "s" : "";
    }

    @Override
    public void resetAbilities(AgentState s) {
        super.resetAbilities(s);
        ReynaState st = (ReynaState) s;
        st.leerCharges = LEER_CHARGES;
        st.leerArmed = false;
        st.dismissEnd = 0;
        st.devourOrb = null;
        st.devourLeft = 0;
    }
}
