package rainy.valo.Client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rainy.valo.Agents.AbilityInfo;
import rainy.valo.Agents.Agent;
import rainy.valo.Agents.AgentRegistry;
import rainy.valo.network.SelectAgentPayload;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentSelectScreen extends Screen {
    private static final String[] SLOT = {"c", "q", "e", "x"};
    private Agent preview;
    private final Map<Agent, ArtButton> tiles = new LinkedHashMap<>();

    public AgentSelectScreen() {
        super(Text.literal("Agent Select"));
        var current = ClientHudState.data;
        Agent cur = current == null ? null : AgentRegistry.get(current.agentId());
        preview = cur != null ? cur : AgentRegistry.all().iterator().next();
    }

    @Override
    protected void init() {
        tiles.clear();
        int y = height / 2 - AgentRegistry.all().size() * 25;
        for (Agent a : AgentRegistry.all()) {
            ArtButton tile = new ArtButton(30, y, 160, 44, a.displayName().toUpperCase(), a.color(),
                    ClientAssets.gui("select/tile"), ClientAssets.gui("select/tile_hover"),
                    ClientAssets.gui("select/tile_selected"), ClientAssets.gui("agent/" + a.id() + "_icon"),
                    () -> preview = a);
            tiles.put(a, tile);
            addDrawableChild(tile);
            y += 50;
        }

        addDrawableChild(new ArtButton(width - 190, height - 56, 160, 40, "LOCK IN", 0xFFFFD34D,
                ClientAssets.gui("select/lock_in"), ClientAssets.gui("select/lock_in_hover"), null, null, () -> {
            ClientPlayNetworking.send(new SelectAgentPayload(preview.id()));
            close();
        }));

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear agent"), b -> {
            ClientPlayNetworking.send(new SelectAgentPayload("none"));
            close();
        }).dimensions(30, height - 40, 110, 20).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!ClientAssets.draw(ctx, ClientAssets.gui("select_background"), 0, 0, width, height)) {
            ctx.fill(0, 0, width, height, 0xFF0B0B12);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        tiles.forEach((agent, tile) -> tile.isSelected = agent == preview);
        super.render(ctx, mouseX, mouseY, delta);
        int accent = preview.color();

        if (!ClientAssets.drawFit(ctx, ClientAssets.gui("logo"), width / 2 - 150, 6, 300, 60)) {
            ctx.drawCenteredTextWithShadow(textRenderer, "AGENT SELECT", width / 2, 14, 0xFFFFFFFF);
        }

        int px = (int) (width * 0.30), py = 70, pw = (int) (width * 0.28), ph = height - 120;
        if (!ClientAssets.drawFit(ctx, ClientAssets.gui("agent/" + preview.id() + "_portrait"), px, py, pw, ph)) {
            ctx.fill(px, py, px + pw, py + ph, 0x66000000 | (accent & 0xFFFFFF));
            ctx.drawCenteredTextWithShadow(textRenderer, preview.displayName().toUpperCase(), px + pw / 2, py + ph / 2, 0xFFFFFFFF);
        }

        int ix = (int) (width * 0.62), iw = (int) (width * 0.34), iy = 76;
        ctx.getMatrices().push();
        ctx.getMatrices().scale(2f, 2f, 1f);
        ctx.drawTextWithShadow(textRenderer, preview.displayName().toUpperCase(), ix / 2, iy / 2, accent);
        ctx.getMatrices().pop();

        int ry = iy + 22;
        Identifier role = ClientAssets.gui("role/" + preview.role());
        boolean hasRole = ClientAssets.draw(ctx, role, ix, ry, 20, 20);
        ctx.drawTextWithShadow(textRenderer, preview.role().toUpperCase(), ix + (hasRole ? 26 : 0), ry + 6, 0xFFBBBBCC);
        iy = ry + 30;

        AbilityInfo[] abilities = preview.abilities();
        for (int i = 0; i < 4; i++) {
            Identifier icon = ClientAssets.gui("ability/" + preview.id() + "_" + SLOT[i]);
            if (!ClientAssets.draw(ctx, icon, ix, iy, 32, 32)) {
                ctx.fill(ix, iy, ix + 32, iy + 32, 0x88000000 | (accent & 0xFFFFFF));
            }
            ctx.drawTextWithShadow(textRenderer, abilities[i].name() + (i == 3 ? "  (ULTIMATE)" : ""), ix + 40, iy, accent);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(abilities[i].description()), iw - 40);
            int ly = iy + 11;
            for (OrderedText line : lines) {
                ctx.drawTextWithShadow(textRenderer, line, ix + 40, ly, 0xFFBBBBCC);
                ly += 10;
            }
            iy += Math.max(40, 12 + lines.size() * 10 + 6);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
