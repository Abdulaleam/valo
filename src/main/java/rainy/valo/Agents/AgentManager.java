package rainy.valo.Agents;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import rainy.valo.network.AgentSyncPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AgentManager {
    private static final int SYNC_INTERVAL = 5;
    private static final Map<UUID, AgentState> STATES = new HashMap<>();

    private AgentManager() {

    }

    public static void init() {

        ServerLivingEntityEvents.AFTER_DEATH.register(AgentManager::onDeath);

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity p) {
                AgentState s = STATES.get(p.getUuid());
                if (s != null && p.getServer() != null) {
                    return s.agent.allowDamage(p, source, s, p.getServer().getTicks());
                }
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(AgentManager::tick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity p = handler.getPlayer();
            Agent saved = AgentRegistry.get(AgentStorage.get(server).get(p.getUuid()));
            if (saved != null) select(p, saved, false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                STATES.remove(handler.getPlayer().getUuid()));
    }

    public static AgentState getState(ServerPlayerEntity p) {
        return STATES.get(p.getUuid());
    }
    public static void select(ServerPlayerEntity p, Agent agent, boolean announce) {
        AgentState old = STATES.get(p.getUuid());
        if (old != null) old.agent.onDeselect(p, old);

        AgentState state = agent.createState();
        STATES.put(p.getUuid(), state);
        agent.onSelect(p, state);
        AgentStorage.get(p.getServer()).set(p.getUuid(), agent.id());
        sync(p, state, p.getServer().getTicks());

        if (announce) {
            AbilityInfo[] a = agent.abilities();
            p.sendMessage(Text.literal("Agent: " + agent.displayName() + "  |  "
                    + a[0].name() + " / " + a[1].name() + " / " + a[2].name() + " / " + a[3].name()), false);
        }
    }

    public static void deselect(ServerPlayerEntity p) {
        AgentState old = STATES.remove(p.getUuid());
        if (old != null)
            old.agent.onDeselect(p, old);
        AgentStorage.get(p.getServer()).remove(p.getUuid());
        ServerPlayNetworking.send(p, AgentSyncPayload.empty());
        p.sendMessage(Text.literal("Agent cleared."), false);
    }

    public static void use(ServerPlayerEntity p, int slot) {
        if (slot < 0 || slot > 3)
            return;
        AgentState s = STATES.get(p.getUuid());
        if (s == null) {
            p.sendMessage(Text.literal("Pick an agent First: /valo agent name"), true);
            return;
        }
        int now = p.getServer().getTicks();
        Agent agent = s.agent;

        if (slot == 3 && s.isUltActive(now)) {
            agent.useActiveUlt(p, s, now);
            sync(p, s, now);
            return;
        }

        int cd = s.cooldownRemaining(slot, now);
        if (cd > 0) {
            p.sendMessage(Text.literal(agent.abilities()[slot].name() + " ready in " + ((cd + 19) / 20) + "s"), true);
            return;
        }
        if (slot == 3) {
            if (s.ultPoints < agent.ultCost()) {
                p.sendMessage(Text.literal(agent.abilities()[3].name() + ": " + s.ultPoints + "/" + agent.ultCost()), true);
                return;
            }
            if (agent.use(p, slot, s , now))
                s.ultPoints = 0;
        } else {
            agent.use(p, slot, s, now);
        }
        sync(p, s, now);
    }
    private static void onDeath(LivingEntity victim, DamageSource source) {
        if (victim instanceof ServerPlayerEntity vp) {
            AgentState vs = STATES.get(vp.getUuid());
            if (vs != null)
                vs.activeEnd = 0;
        }
        if (!(source.getAttacker() instanceof ServerPlayerEntity killer) || killer == victim) return;
        AgentState s = STATES.get(killer.getUuid());
        if (s == null)
            return;
        int now =killer.getServer().getTicks();
        Agent agent = s.agent;

        if (!s.isUltActive(now) && s.ultPoints < agent.ultCost()) {
            s.ultPoints++;
            killer.sendMessage(Text.literal(s.ultPoints >= agent.ultCost()
                    ? agent.abilities()[3].name() + " ready!" : "Ult " + s.ultPoints + "/" + agent.ultCost()), true);
        }
        agent.onKill(killer, victim, s, now);
        sync(killer, s, now);
    }

    private static void tick(MinecraftServer server) {
        int now = server.getTicks();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            AgentState s = STATES.get(p.getUuid());
            if (s == null)
                continue;

            boolean active = s.isUltActive(now);
            if (s.wasActive && !active)
                s.agent.onUltEnd(p, s, now);
            s.wasActive = active;

            s.agent.onTick(p, s, now);
            if (now % SYNC_INTERVAL == 0)
                sync(p, s, now);
        }

    }
    public static void debugFillUlt(ServerPlayerEntity p) {
        AgentState s = STATES.get(p.getUuid());
        if ( s == null)
            return;
        s.ultPoints = s.agent.ultCost();
        sync(p,s, p.getServer().getTicks());

    }
    public static void debugReset(ServerPlayerEntity p) {
        AgentState s = STATES.get(p.getUuid());
        if (s == null )
            return;
        s.agent.resetAbilities(s);
        sync(p, s , p.getServer().getTicks());
    }
    public static void sync(ServerPlayerEntity p, AgentState s, int now) {
        Agent a = s.agent;
        int[] cdRemaining = new int[4];
        int[] cdTotal = new int[4];
        int[] charges = new int[4];
        for (int i = 0; i < 4; i++) {
            SlotView v = a.slotView(p, i, s, now);
            cdRemaining[i] = v.cooldownRemaining();
            cdTotal[i] = v.cooldownTotal();
            charges[i] = v.charges();
        }
        ServerPlayNetworking.send(p, new AgentSyncPayload(a.id(), s.ultPoints, a.ultCost(),
                s.activeRemaining(now), s.activeTotal, a.status(p, s, now), cdRemaining, cdTotal, charges));
    }
}