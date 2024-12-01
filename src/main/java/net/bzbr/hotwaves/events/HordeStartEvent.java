package net.bzbr.hotwaves.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

@FunctionalInterface
public interface HordeStartEvent {

    void onHordeStartEvent(String message, MinecraftServer minecraftServer, int currentDayNumber);

    // Экземпляр события, который будет вызываться
    Event<HordeStartEvent> EVENT = EventFactory.createArrayBacked(HordeStartEvent.class,
            (listeners) -> (message, minecraftServer, currentDayNumber) -> {
                for (HordeStartEvent listener : listeners) {
                    listener.onHordeStartEvent(message, minecraftServer, currentDayNumber);
                }
            }
    );
}
