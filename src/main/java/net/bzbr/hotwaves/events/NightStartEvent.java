package net.bzbr.hotwaves.events;

import net.bzbr.hotwaves.data.ServerTimePersistentState;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

@FunctionalInterface
public interface NightStartEvent {

    void onNightStartEvent(MinecraftServer minecraftServer, ServerTimePersistentState serverTimePersistentState, boolean shouldWaveContinue);

    // Экземпляр события, который будет вызываться
    Event<NightStartEvent> EVENT = EventFactory.createArrayBacked(NightStartEvent.class,
            (listeners) -> (minecraftServer, serverTimePersistentState, shouldWaveContinue) -> {
                for (NightStartEvent listener : listeners) {
                    listener.onNightStartEvent(minecraftServer, serverTimePersistentState, shouldWaveContinue);
                }
            }
    );
}
