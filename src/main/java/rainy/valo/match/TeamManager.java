package rainy.valo.match;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class TeamManager {
    private TeamManager() {
    }

    public static Team team(MinecraftServer server, Sides sides) {

        Scoreboard sb = server.getScoreboard();
        Team t = sb.getTeam(sides.teamName);
        if (t == null) {
            t = sb.addTeam(sides.teamName);
            t.setDisplayName(Text.literal(sides.label));
            t.setColor(sides.color);
            t.setFriendlyFireAllowed(false);
        }
        return t;
    }

    public static void join(MinecraftServer server, ServerPlayerEntity p, Sides sides) {
        leave(server, p);
        server.getScoreboard().addScoreHolderToTeam(p.getNameForScoreboard(), team(server, sides));
    }

    public static void leave(MinecraftServer server, ServerPlayerEntity p) {
        Scoreboard sb = server.getScoreboard();
        Team t = sb.getScoreHolderTeam(p.getNameForScoreboard());
        if (t != null && t.getName().startsWith("valo_")) {
            sb.removeScoreHolderFromTeam(p.getNameForScoreboard(), t);
        }
    }

    public static Sides sideOf(MinecraftServer server, ServerPlayerEntity p) {
        Team t = server.getScoreboard().getScoreHolderTeam(p.getNameForScoreboard());
        if (t == null)
            return null;
        for (Sides sides : Sides.values()) {
            if (sides.teamName.equals(t.getName()))
                return sides;
        }
        return null;
    }

    public static List<ServerPlayerEntity> members(MinecraftServer server, Sides sides) {
        List<ServerPlayerEntity> out = new ArrayList<>();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (sideOf(server, p) == sides)
                out.add(p);
        }
        return out;
    }

}
