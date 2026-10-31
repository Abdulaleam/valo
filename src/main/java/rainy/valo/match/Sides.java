package rainy.valo.match;

import net.minecraft.util.Formatting;

public enum Sides {
    ATTACKERS("valo_attackers", "attackers", "Attackers", Formatting.RED),
    DEFENDERS("valo_defenders", "defenders", "Defenders", Formatting.AQUA);

    public final String teamName;
    public final String id;
    public final String label;
    public final Formatting color;


    Sides(String teamName, String id, String label, Formatting color) {
        this.teamName = teamName;
        this.id = id;
        this.label = label;
        this.color = color;
    }

    public static Sides parse(String s) {
        for (Sides sides : values()) {
            if (sides.id.equalsIgnoreCase(s))
                return sides;
        }
        return null;
    }
}
