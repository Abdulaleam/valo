package rainy.valo;

import rainy.valo.Client.*;
import rainy.valo.Entity.LeerEntity;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.AbilityPayLoad;
import rainy.valo.network.AgentSyncPayload;
import rainy.valo.network.HudEventPayload;
import rainy.valo.network.HudEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ValoClient implements ClientModInitializer {
    private static final String[] NAMES = {"slot_c", "slot_q", "slot_e", "slot_x"};
    private static final int[] DEFAULT_KEYS = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V};

    private static final KeyBinding[] KEYS = new KeyBinding[4];
    private static KeyBinding selectKey;

    public static String keyLabel(int slot) {
        return KEYS[slot] == null ? "?" : KEYS[slot].getBoundKeyLocalizedText().getString();
    }

    @Override
    public void onInitializeClient() {
        for (int i = 0; i < 4; i++) {
            KEYS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.valo." + NAMES[i], InputUtil.Type.KEYSYM, DEFAULT_KEYS[i], "category.valo"));
        }
        selectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.valo.agent_select", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.valo"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            for (int i = 0; i < 4; i++) {
                while (KEYS[i].wasPressed()) {
                    ClientPlayNetworking.send(new AbilityPayLoad(i));
                }
            }
            while (selectKey.wasPressed()) {
                client.setScreen(new AgentSelectScreen());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(AgentSyncPayload.ID,
                (payload, context) -> ClientHudState.update(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientHudState.clear());
        HudRenderCallback.EVENT.register(ValorantHud::render);

        ClientPlayNetworking.registerGlobalReceiver(HudEventPayload.ID, (payload, context) -> {
            switch (payload.type()) {
                case HudEvents.KILL -> ClientBanners.kill(payload.value());
                case HudEvents.HIT -> ClientBanners.hit();
                default -> { }
            }
        });

        ParticleSetup.init();

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(Valo.MOD_ID, "asset_cache");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        ClientAssets.clearCache();
                    }
                });

        Identifier orbTex = Identifier.of(Valo.MOD_ID, "textures/entity/soul_orb.png");
        Identifier eyeTex = Identifier.of(Valo.MOD_ID, "textures/entity/leer_eye.png");
        EntityRendererRegistry.register(ModEntities.SOUL_ORB,
                ctx -> new BillboardRenderer<SoulOrbEntity>(ctx, orbTex, 0.6f, 0.08f));
        EntityRendererRegistry.register(ModEntities.LEER_EYE,
                ctx -> new BillboardRenderer<LeerEntity>(ctx, eyeTex, 0.9f, 0.03f));
    }
}
