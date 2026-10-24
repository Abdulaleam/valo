package rainy.valo.Agents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import rainy.valo.Entity.LeerEntity;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.HudEvents;
import rainy.valo.particles.ValoParticles;

public class ReynaAgent extends Agent {
    public static final ReynaAgent INSTANCE = new ReynaAgent();

    static final double ORB_RANGE = 10.0;
    static final int LEER_COOLDOWN = 400;
    static final int DEVOUR_COOLDOWN = 160;
    static final int DEVOUR_COOLDOWN_WITH_ORB = 60;
    static final int DISMISS_COOLDOWN = 240;
    static final int DISMISS_COOLDOWN_WITH_ORB = 120;
    static final int DISMISS_DURATION = 20;
    static final int DISMISS_DURATION_WITH_ORB = 40;
    static final int EMPRESS_DURATION = 600;

    private static final AbilityInfo[] ABILITIES = {
            new AbilityInfo("Leer", "Equip the eye, then left click to throw it where you aim. Nearsights anyone looking at it. Destructible."),
            new AbilityInfo("Devour", "Heal over time. A nearby soul orb makes it much stronger and faster to reuse."),
            new AbilityInfo("Dismiss", "Become invisible and untouchable. A nearby soul orb doubles the duration."),
            new AbilityInfo("Empress", "30s of speed, haste, strength and night vision. Kills renew it.")
    };

    private static final class ReynaState extends AgentState {
        int dismissEnd;
        boolean leerArmed;

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
            case 0 -> leer(p, st, now);
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

    private boolean leer(ServerPlayerEntity p, ReynaState s, int now) {
        if (s.leerArmed) {
            disarm(p, s);
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
        if (!st.leerArmed) return false;

        ServerWorld world = p.getServerWorld();
        Vec3d look = p.getRotationVec(1f);

        LeerEntity eye = new LeerEntity(ModEntities.LEER_EYE, world);
        eye.setOwner(p.getUuid());
        eye.setPosition(p.getEyePos().add(look.multiply(0.6)).subtract(0, 0.3, 0));
        eye.setVelocity(look.multiply(LeerEntity.SPEED));
        world.spawnEntity(eye);

        st.startCooldown(0, LEER_COOLDOWN, now);
        disarm(p, st);
        world.playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.PLAYERS, 1f, 1.5f);
        return true;
    }

    private boolean devour(ServerPlayerEntity p, ReynaState s, int now) {
        boolean orb = takeOrb(p);
        boolean strong = orb || s.isUltActive(now);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, strong ? 4 : 2));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 200, strong ? 1 : 0));
        p.getServerWorld().spawnParticles(ValoParticles.DEVOUR_HEAL, p.getX(), p.getY() + 0.2, p.getZ(), strong ? 8 : 4, 0.4, 0.1, 0.4, 0.04);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_WITCH_DRINK, SoundCategory.PLAYERS, 1f, 0.8f);
        s.startCooldown(1, strong ? DEVOUR_COOLDOWN_WITH_ORB : DEVOUR_COOLDOWN, now);
        return true;
    }

    private boolean dismiss(ServerPlayerEntity p, ReynaState s, int now) {
        boolean orb = takeOrb(p);
        boolean strong = orb || s.isUltActive(now);
        int duration = strong ? DISMISS_DURATION_WITH_ORB : DISMISS_DURATION;
        s.dismissEnd = now + duration;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 1));
        p.getServerWorld().spawnParticles(ValoParticles.DISMISS_BURST, p.getX(), p.getY() + 1, p.getZ(), 8, 0.4, 0.8, 0.4, 0.05);
        p.getServerWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1.2f);
        s.startCooldown(2, strong ? DISMISS_COOLDOWN_WITH_ORB : DISMISS_COOLDOWN, now);
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
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, EMPRESS_DURATION, 0));
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

    private boolean takeOrb(ServerPlayerEntity p) {
        SoulOrbEntity orb = nearestOrb(p);
        if (orb == null) return false;
        ServerWorld world = p.getServerWorld();
        Vec3d from = orb.getPos().add(0, 0.3, 0);
        Vec3d to = p.getPos().add(0, 1, 0);
        for (int i = 0; i <= 10; i++) {
            Vec3d v = from.lerp(to, i / 10.0);
            world.spawnParticles(ValoParticles.SOUL_WISP, v.x, v.y, v.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        orb.discard();
        return true;
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
        if (s.isUltActive(now) && now % 3 == 0) {
            p.getServerWorld().spawnParticles(ValoParticles.EMPRESS_AURA, p.getX(), p.getY() + 1.0, p.getZ(), 2,
                    0.4, 0.7, 0.4, 0.02);
        }
    }

    @Override
    public void onDeselect(ServerPlayerEntity p, AgentState s) {
        HudEvents.send(p, HudEvents.ARM, 0);
    }

    @Override
    public boolean allowDamage(ServerPlayerEntity p, DamageSource source, AgentState s, int now) {
        return ((ReynaState) s).dismissEnd <= now;
    }

    @Override
    public SlotView slotView(ServerPlayerEntity p, int slot, AgentState s, int now) {
        if (slot == 1 || slot == 2) {
            int orbs = p.getServerWorld().getEntitiesByClass(SoulOrbEntity.class,
                    p.getBoundingBox().expand(ORB_RANGE), o -> p.getUuid().equals(o.getOwnerUuid())).size();
            return new SlotView(s.cooldownRemaining(slot, now), s.cooldownTotal[slot], orbs > 0 ? orbs : -1);
        }
        return super.slotView(p, slot, s, now);
    }

    @Override
    public String status(ServerPlayerEntity p, AgentState s, int now) {
        if (((ReynaState) s).leerArmed) return "LEER: LEFT CLICK TO THROW";
        return s.isUltActive(now) ? "EMPRESS " + ((s.activeRemaining(now) + 19) / 20) + "s" : "";
    }

}
