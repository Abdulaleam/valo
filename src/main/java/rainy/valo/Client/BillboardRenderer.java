package rainy.valo.Client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Draws an entity as a glowing camera-facing sprite that bobs and pulses. */
public class BillboardRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final Identifier texture;
    private final float size;
    private final float bob;

    public BillboardRenderer(EntityRendererFactory.Context ctx, Identifier texture, float size, float bob) {
        super(ctx);
        this.texture = texture;
        this.size = size;
        this.bob = bob;
        this.shadowRadius = 0f;
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        float t = entity.age + tickDelta;
        float pulse = 1f + 0.08f * MathHelper.sin(t * 0.4f);

        matrices.push();
        matrices.translate(0, size / 2f + MathHelper.sin(t * 0.2f) * bob, 0);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(size * pulse, size * pulse, size * pulse);

        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(texture));
        vertex(vc, entry, -0.5f, -0.5f, 0f, 1f);
        vertex(vc, entry, 0.5f, -0.5f, 1f, 1f);
        vertex(vc, entry, 0.5f, 0.5f, 1f, 0f);
        vertex(vc, entry, -0.5f, 0.5f, 0f, 0f);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void vertex(VertexConsumer vc, MatrixStack.Entry entry, float x, float y, float u, float v) {
        vc.vertex(entry, x, y, 0f)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(FULL_BRIGHT)
                .normal(entry, 0f, 1f, 0f);
    }
}
