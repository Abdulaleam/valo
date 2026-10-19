package rainy.valo;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import rainy.valo.Agents.Agent;
import rainy.valo.Agents.AgentManager;
import rainy.valo.Agents.AgentRegistry;
import rainy.valo.network.AgentSyncPayload;
import rainy.valo.network.HudEventPayload;
import rainy.valo.network.SelectAgentPayload;
import rainy.valo.particles.ValoParticles;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.AbilityPayLoad;

public class Valo implements ModInitializer {
	public static final String MOD_ID = "valo";

	public static final EntityType<SoulOrbEntity> SOUL_ORB = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of(MOD_ID, "soul_orb"),
			EntityType.Builder.create(SoulOrbEntity::new, SpawnGroup.MISC)
					.dimensions(0.5f, 0.5f).maxTrackingRange(10).makeFireImmune().build("soul_orb"));

	@Override
	public void onInitialize() {
		ValoParticles.registerValoParticles();
		ModEntities.registerEntities();
		AgentManager.init();

		PayloadTypeRegistry.playC2S().register(AbilityPayLoad.ID, AbilityPayLoad.CODEC);
		PayloadTypeRegistry.playS2C().register(AgentSyncPayload.ID, AgentSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(HudEventPayload.ID, HudEventPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectAgentPayload.ID, SelectAgentPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SelectAgentPayload.ID, (payload, context) -> {
			if (payload.id().equals("none")) {
				AgentManager.deselect(context.player());
				return;
			}
			Agent agent = AgentRegistry.get(payload.id());
			if (agent != null) AgentManager.select(context.player(), agent, true);
		});

		ServerPlayNetworking.registerGlobalReceiver(AbilityPayLoad.ID,
				((payload, context) -> AgentManager.use(context.player(), payload.slot())));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("valorant")
						.then(CommandManager.literal("agent")
								.then(CommandManager.literal("none").executes(ctx -> {
									AgentManager.deselect(ctx.getSource().getPlayerOrThrow());
									return 1;
								}))
								.then(CommandManager.argument("id", StringArgumentType.word())
										.suggests((ctx, builder) -> CommandSource.suggestMatching(AgentRegistry.ids(), builder))
										.executes(ctx -> {
											String id = StringArgumentType.getString(ctx, "id");
											Agent agent = AgentRegistry.get(id);
											if (agent == null) {
												ctx.getSource().sendError(Text.literal("We Aint got that Agent bro '" + id + "'. Try /valorant list"));
												return 0;
											}
											AgentManager.select(ctx.getSource().getPlayerOrThrow(), agent, true);
											return 1;
										})))));

	}

}