package rainy.valo.Agents;

import java.util.Arrays;

public class AgentState {
    public final Agent agent;
    public int ultPoints;
    public int activeEnd;
    public int activeTotal;
    public boolean wasActive;
    public final int[] cooldownEnd = new int[4];
    public final int[] cooldownTotal = new int[4];

    public AgentState(Agent agent) {
        this.agent = agent;
    }
    public boolean isUltActive(int now) {
        return activeEnd > now;
    }
    public int activeRemaining(int now) {
        return Math.max(0, activeEnd - now);
    }
    public void startCooldown(int slot, int ticks, int now) {
        cooldownEnd[slot] = now + ticks;
        cooldownTotal[slot] = ticks;
    }

    public int cooldownRemaining(int slot, int now) {
        return Math.max(0, cooldownEnd[slot] - now);
    }
    public void clearCooldowns() {
        Arrays.fill(cooldownEnd, 0);
    }

}