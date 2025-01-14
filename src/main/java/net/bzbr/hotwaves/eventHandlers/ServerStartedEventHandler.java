package net.bzbr.hotwaves.eventHandlers;

import net.bzbr.hotwaves.Hotwaves;
import net.bzbr.hotwaves.data.ServerTimePersistentState;
import net.bzbr.hotwaves.events.DayStartEvent;
import net.bzbr.hotwaves.events.HordeEndEvent;
import net.bzbr.hotwaves.events.NightStartEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

public class ServerStartedEventHandler {



    private ServerTimePersistentState serverTimePersistentState;

    private ServerWorld _overworld;
    private int tickCount;
    private final int checkInterval = 100;
    private Logger logger;
    private boolean isDay;
    private int dayNumber;
    private boolean shouldWaveContinue;


    public void register(MinecraftServer server) {

        this.logger = Hotwaves.LOGGER;

        _overworld = server.getOverworld();

        var manager = _overworld.getPersistentStateManager();

        serverTimePersistentState = manager.getOrCreate(
                ServerTimePersistentState::fromNbt,
                ServerTimePersistentState::new,
                Hotwaves.GET_SERVER_DAY_STATE_IDENTIFIER.toString());

        tickCount = 0;
        isDay = _overworld.isDay();
        dayNumber = serverTimePersistentState.getDayNumber();
        shouldWaveContinue = serverTimePersistentState.getIsWaveRunning();
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
        this.logger.info("ServerStartedEventHandler initialized");
        this.logger.info("Is day now: {}", isDay);
        this.logger.info("Day number: {}", dayNumber);
    }

    private void onServerTick(MinecraftServer minecraftServer) {

        if (tickCount >= checkInterval) {


            tickCount = 0;
            if ((isDay && _overworld.isNight()) || (shouldWaveContinue && !isDay)) {

                if (shouldWaveContinue){
                    this.logger.info("Horde continue");
                }

                NightStartEvent.EVENT.invoker().onNightStartEvent(minecraftServer, serverTimePersistentState, shouldWaveContinue);
                shouldWaveContinue = false;
                serverTimePersistentState.setIsDay(false);
                isDay = _overworld.isDay();
            }

            if (!isDay && _overworld.isDay()) {
                DayStartEvent.EVENT.invoker().onDayStartEvent("Day is started");
                HordeEndEvent.EVENT.invoker().onHordeEndEvent();
                serverTimePersistentState.incrementDayNumber();
                serverTimePersistentState.setIsDay(true);
                dayNumber = serverTimePersistentState.getDayNumber();
                isDay = serverTimePersistentState.getIsDay();
                this.logger.info("Horde ended");
            }
        }

        tickCount++;
    }
}
