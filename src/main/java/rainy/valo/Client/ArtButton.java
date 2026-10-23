package rainy.valo.Client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ArtButton extends ClickableWidget {
    private final String label;
    private final int accent;
    private final Identifier normal, hover, selected, icon;
    private final Runnable onPress;
    public boolean isSelected;

    public ArtButton(int x, int y, int w, int h, String label, int accent,
                     Identifier normal, Identifier hover, Identifier selected, Identifier icon, Runnable onPress) {
        super(x, y, w, h, Text.literal(label));
        this.label = label;
        this.accent = accent;
        this.normal = normal;
        this.hover = hover;
        this.selected = selected;
        this.icon = icon;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        Identifier bg = (isSelected && selected != null) ? selected
                : (isHovered() && hover != null) ? hover : normal;
        if (bg == null || !ClientAssets.draw(ctx, bg, x, y, w, h)) {
            int fill = isSelected ? (0xAA000000 | (accent & 0xFFFFFF)) : isHovered() ? 0xAA333355 : 0xAA15151F;
            ctx.fill(x, y, x + w, y + h, fill);
            ctx.drawBorder(x, y, w, h, isSelected ? accent : 0xFF555566);
        }

        boolean hasIcon = icon != null && ClientAssets.has(icon);
        if (hasIcon) ClientAssets.draw(ctx, icon, x + 6, y + (h - 32) / 2, 32, 32);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int tx = hasIcon ? x + 44 : x + (w - tr.getWidth(label)) / 2;
        ctx.drawTextWithShadow(tr, label, tx, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }
}