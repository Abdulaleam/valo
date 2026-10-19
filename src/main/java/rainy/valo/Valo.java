package rainy.valo;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import rainy.valo.Agents.Agent;
import rainy.valo.Agents.AgentManager;
import rainy.valo.Agents.AgentRegistry;
import rainy.valo.Entity.ModEntities;
import rainy.valo.Entity.SoulOrbEntity;
import rainy.valo.network.AbilityPayLoad;
import rainy.valo.network.AgentSyncPayload;
import rainy.valo.network.HudEventPayload;
import rainy.valo.network.HudEvents;
import rainy.valo.network.SelectAgentPayload;
import rainy.valo.particles.ValoParticles;

public class Valo implements ModInitializer {
	public static final String MOD_ID = "valo";

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
										})))
						.then(CommandManager.literal("list").executes(ctx -> {
							for (Agent a : AgentRegistry.all()) {
								StringBuilder sb = new StringBuilder(a.displayName()).append(" (" + a.id() + ")");
								for (var ab : a.abilities())
									sb.append("\n  ").append(ab.name()).append(": ").append(ab.description());
								String text = sb.toString();
								ctx.getSource().sendFeedback(() -> Text.literal(text), false);
							}
							return 1;
						}))
						.then(CommandManager.literal("debug")
								.requires(src -> src.hasPermissionLevel(2))
								.then(CommandManager.literal("ult").executes(ctx -> {
									AgentManager.debugFillUlt(ctx.getSource().getPlayerOrThrow());
									return 1;
								}))
								.then(CommandManager.literal("reset").executes(ctx -> {
									AgentManager.debugReset(ctx.getSource().getPlayerOrThrow());
									return 1;
								}))
								.then(CommandManager.literal("particles").executes(ctx -> {
									ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
									ServerWorld w = p.getServerWorld();
									Vec3d look = p.getRotationVec(1f);
									Vec3d right = new Vec3d(-look.z, 0, look.x).normalize();
									Vec3d base = p.getEyePos().add(look.multiply(4));
									int n = ValoParticles.all().size(), i = 0;
									for (var e : ValoParticles.all().entrySet()) {
										Vec3d pos = base.add(right.multiply((i - (n - 1) / 2.0) * 0.8));
										w.spawnParticles(e.getValue(), pos.x, pos.y, pos.z, 3, 0.05, 0.05, 0.05, 0.0);
										i++;
									}
									String names = String.join(", ", ValoParticles.all().keySet());
									ctx.getSource().sendFeedback(() -> Text.literal("Left to right: " + names), false);
									return 1;
								}))
								.then(CommandManager.literal("banner")
										.then(CommandManager.argument("streak", IntegerArgumentType.integer(1, 5))
												.executes(ctx -> {
													HudEvents.send(ctx.getSource().getPlayerOrThrow(), HudEvents.KILL,
															IntegerArgumentType.getInteger(ctx, "streak"));
													return 1;
												})))
								.then(CommandManager.literal("hit").executes(ctx -> {
									HudEvents.send(ctx.getSource().getPlayerOrThrow(), HudEvents.HIT, 0);
									return 1;
								}))
								.then(CommandManager.literal("orb").executes(ctx -> {
									ServerPlayerEntity p = ctx.getSource().getPlayerOrThrow();
									SoulOrbEntity orb = new SoulOrbEntity(ModEntities.SOUL_ORB, p.getServerWorld());
									orb.setOwner(p.getUuid());
									orb.setPosition(p.getEyePos().add(p.getRotationVec(1f).multiply(3)).subtract(0, 0.8, 0));
									p.getServerWorld().spawnEntity(orb);
									return 1;
								})))));
	}
}