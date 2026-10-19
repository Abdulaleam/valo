package rainy.valo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import rainy.valo.Client.BillboardRenderer;
import rainy.valo.Entity.LeerEntity;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.AbilityPayLoad;

public class ValoClient implements ClientModInitializer {
    private static final String[] NAMES = {"leer", "devour", "dismiss", "empress"};
    private static final int[] DEFAULT_KEYS = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V};

    private final KeyBinding[] keys = new KeyBinding[4];

    @Override
    public void onInitializeClient() {
        for (int i = 0; i < 4; i++) {
            keys[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.valo." + NAMES[i], InputUtil.Type.KEYSYM, DEFAULT_KEYS[i], "category.valo"));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            for (int i = 0; i < 4; i++) {
                while (keys[i].wasPressed()) {
                    ClientPlayNetworking.send(new AbilityPayLoad(i));
                }
            }
        });

        Identifier eyeTex = Identifier.of(Valo.MOD_ID, "textures/entity/leer_eye.png");
        Identifier orbTex = Identifier.of(Valo.MOD_ID, "textures/entity/soul_orb.png");

        EntityRendererRegistry.register(ModEntities.LEER_EYE,
                ctx -> new BillboardRenderer<LeerEntity>(ctx, eyeTex, 0.9f, 0.03f));
        EntityRendererRegistry.register(Valo.SOUL_ORB,
                ctx -> new BillboardRenderer<SoulOrbEntity>(ctx, orbTex, 0.6f, 0.08f));
    }
}