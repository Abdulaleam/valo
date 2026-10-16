package rainy.valo;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Valo implements ModInitializer {
	public static final String MOD_ID = "valo";


	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(AbilityPayLoad.ID, AbilityPayLoad.CODEC);


		ServerPlayNetworking.registerGlobalReceiver(AbilityPayLoad.ID, ((payload, context) ->
				Reyna.use(context.player(), payload.slot())));

		Reyna.init();


		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("valo")
						.then(CommandManager.literal("agent")
								.then(CommandManager.literal("reyna").executes(ctx -> {
									Reyna.select(ctx.getSource().getPlayerOrThrow());
									return 1;
								}))
								.then(CommandManager.literal("none").executes(ctx -> {
									Reyna.deselect(ctx.getSource().getPlayerOrThrow());
								}))))));



	}
}
