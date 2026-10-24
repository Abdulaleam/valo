package rainy.valo.Agents;

public final class ChargePool {
    public final int max;
    public final int regenTicks;
    public int charges;
    public int regenEnd;

    public ChargePool(int max, int regenTicks) {
        this.max = max;
        this.regenTicks = regenTicks;
        this.charges = max;
    }

    public boolean tryUse(int now) {
        if (charges <= 0)
            return false;
        if (charges == max)
            regenEnd = now + regenTicks;
        charges--;
        return true;
    }

    public void tick(int now) {
        if (charges < max && now >= regenEnd) {
            charges++;
            if (charges < max)
                regenEnd  = now + regenTicks;
        }
    }

    public int remaining(int now) {

        return charges < max ? Math.max(0, regenEnd - now) : 0;

    }

    public void refill() {

        charges = max;
    }
}
