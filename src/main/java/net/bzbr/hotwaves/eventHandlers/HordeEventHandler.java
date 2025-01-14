package net.bzbr.hotwaves.eventHandlers;

import net.bzbr.hotwaves.Hotwaves;
import net.bzbr.hotwaves.common.business.WaveSpawner;
import net.bzbr.hotwaves.data.ServerTimePersistentState;
import net.minecraft.server.MinecraftServer;

public class HordeEventHandler {

    private WaveSpawner waveSpawner;

    public void onStartHorde(String message, MinecraftServer minecraftServer, int currentDayNumber) {

        onEndHorde();
        waveSpawner = new WaveSpawner(minecraftServer, currentDayNumber);
        waveSpawner.StartSpawn();
    }

    public void onEndHorde() {


        if (waveSpawner != null) {

            waveSpawner.StopSpawn();
            waveSpawner = null;
        }
    }
}
