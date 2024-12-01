package net.bzbr.hotwaves.eventHandlers;

import net.bzbr.hotwaves.Hotwaves;
import net.bzbr.hotwaves.common.business.WaveSpawner;
import net.bzbr.hotwaves.data.ServerTimePersistentState;
import net.minecraft.server.MinecraftServer;

public class HordeEventHandler {

    private WaveSpawner waveSpawner;
    private ServerTimePersistentState serverTimePersistentState;

    public void onStartHorde(String message, MinecraftServer minecraftServer, int currentDayNumber) {

        var manager = minecraftServer.getOverworld().getPersistentStateManager();

        serverTimePersistentState = manager.getOrCreate(
                ServerTimePersistentState::fromNbt,
                ServerTimePersistentState::new,
                Hotwaves.GET_SERVER_DAY_STATE_IDENTIFIER.toString());

        serverTimePersistentState.setIsWaveRunning(true);
        waveSpawner = new WaveSpawner(minecraftServer, currentDayNumber);
        waveSpawner.StartSpawn();
    }

    public void onEndHorde() {

        serverTimePersistentState.setIsWaveRunning(false);
        if (waveSpawner != null) {

            waveSpawner.StopSpawn();
            waveSpawner = null;
        }
    }
}
