package net.bzbr.hotwaves;

import net.bzbr.hotwaves.common.configuration.ConfigManager;
import net.bzbr.hotwaves.common.registrators.CommandsRegistrator;
import net.bzbr.hotwaves.common.sounds.HordeSounds;
import net.bzbr.hotwaves.eventHandlers.HordeEventHandler;
import net.bzbr.hotwaves.eventHandlers.NightStartEventHandler;
import net.bzbr.hotwaves.eventHandlers.ServerStartedEventHandler;
import net.bzbr.hotwaves.events.HordeEndEvent;
import net.bzbr.hotwaves.events.HordeStartEvent;
import net.bzbr.hotwaves.events.NightStartEvent;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hotwaves implements ModInitializer {
	public static final String MOD_ID = "hotwaves";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier GET_SERVER_DAY_STATE_IDENTIFIER = new Identifier("hordeofthefallen", "horde_persistent_data");

	@Override
	public void onInitialize() {

		ConfigManager.loadConfig();
		HordeSounds.RegisterSounds();
		var hordeEventHandler = new HordeEventHandler();

		NightStartEvent.EVENT.register(new NightStartEventHandler()::onStartNight);
		HordeStartEvent.EVENT.register(hordeEventHandler::onStartHorde);
		HordeEndEvent.EVENT.register(hordeEventHandler::onEndHorde);
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		CommandsRegistrator.Register();
		LOGGER.info("Hello Fabric world!");
	}

	private void onServerStarted(MinecraftServer minecraftServer) {

		var eventHandler = new ServerStartedEventHandler();
		eventHandler.register(minecraftServer);
	}
}