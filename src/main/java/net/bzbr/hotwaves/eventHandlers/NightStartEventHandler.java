package net.bzbr.hotwaves.eventHandlers;

import net.bzbr.hotwaves.Hotwaves;
import net.bzbr.hotwaves.common.configuration.ConfigManager;
import net.bzbr.hotwaves.data.ServerTimePersistentState;
import net.bzbr.hotwaves.events.HordeStartEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

public class NightStartEventHandler {

    public NightStartEventHandler() {

    }

    public void onStartNight(MinecraftServer minecraftServer, ServerTimePersistentState serverTimePersistentState){

        Hotwaves.LOGGER.info("Night started!");
        Hotwaves.LOGGER.info("Current day is: {}", serverTimePersistentState.getDayNumber());
        Hotwaves.LOGGER.info("Check is wave day");

        if (serverTimePersistentState.getDayNumber() % ConfigManager.getWaveIntervalDays() == 0) {
            Hotwaves.LOGGER.info("Today is wave day");
            HordeStartEvent.EVENT.invoker().onHordeStartEvent("Starting wave", minecraftServer, serverTimePersistentState.getDayNumber());
        }
        else {
            Hotwaves.LOGGER.info("Today is not wave day");
        }
    }
}
