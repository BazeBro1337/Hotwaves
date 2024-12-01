package net.bzbr.hotwaves.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

@FunctionalInterface
public interface HordeEndEvent {

    void onHordeEndEvent();

    // Экземпляр события, который будет вызываться
    Event<HordeEndEvent> EVENT = EventFactory.createArrayBacked(HordeEndEvent.class,
            (listeners) -> () -> {
                for (HordeEndEvent listener : listeners) {
                    listener.onHordeEndEvent();
                }
            }
    );
}
