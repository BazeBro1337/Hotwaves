package net.bzbr.hotwaves.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface DayStartEvent {

    void onDayStartEvent(String message);

    // Экземпляр события, который будет вызываться
    Event<DayStartEvent> EVENT = EventFactory.createArrayBacked(DayStartEvent.class,
            (listeners) -> (message) -> {
                for (DayStartEvent listener : listeners) {
                    listener.onDayStartEvent(message);
                }
            }
    );
}
