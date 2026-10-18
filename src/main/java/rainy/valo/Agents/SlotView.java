package rainy.valo.Agents;

public record SlotView(int cooldownRemaining, int cooldownTotal, int charges) {
    public static final SlotView NONE = new SlotView(0, 0, -1);
}