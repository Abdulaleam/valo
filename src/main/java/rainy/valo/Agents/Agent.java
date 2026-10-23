package rainy.valo.Agents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.jmx.Server;

public abstract class Agent {
    public abstract String id();
    public abstract String displayName();
    public String role() {
        return "duelist";
    }
    public abstract int color();
    public abstract int ultCost();
    public abstract AbilityInfo[] abilities();
    public abstract AgentState createState();

    public abstract boolean use(ServerPlayerEntity p, int slot, AgentState s, int now);

    public void useActiveUlt(ServerPlayerEntity p, AgentState s, int now) {}

    public void onSelect(ServerPlayerEntity p, AgentState s) {}
    public void onDeselect(ServerPlayerEntity p, AgentState s) {}
    public void onTick(ServerPlayerEntity p, AgentState s, int now) {}
    public void onKill(ServerPlayerEntity killer, LivingEntity victim, AgentState s, int now) {}
    public void onUltEnd(ServerPlayerEntity p, AgentState s, int now) {}

    public boolean allowDamage(ServerPlayerEntity p, DamageSource source, AgentState s, int now) {
        return true;
    }

    public SlotView slotView(ServerPlayerEntity p, int slot, AgentState s, int now) {
        return new SlotView(s.cooldownRemaining(slot, now), s.cooldownTotal[slot], -1);
    }

    public String status(ServerPlayerEntity p, AgentState s, int now) {
        return "";
    }

    public void resetAbilities(AgentState s) {
        s.clearCooldowns();
    }
}
