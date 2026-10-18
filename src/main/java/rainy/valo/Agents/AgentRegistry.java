package rainy.valo.Agents;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AgentRegistry {
    private static final Map<String, Agent> AGENTS = new LinkedHashMap<>();

    static {


    }
    private AgentRegistry() {

    }
    public static void registerAgent(Agent agent) {
        AGENTS.put(agent.id(), agent);
    }
    public static Agent get(String id) {
        return id == null ? null : AGENTS.get(id);
    }

    public static Collection<Agent> all() {
        return AGENTS.values();
    }
}
