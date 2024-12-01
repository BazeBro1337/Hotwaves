package net.bzbr.hotwaves.common.sounds;

import net.bzbr.hotwaves.Hotwaves;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class HordeSounds {

    public static final SoundEvent HORDE_START_SOUND_1 = RegisterSoundEvent("horde_start_sound_1");
    public static final SoundEvent HORDE_START_SOUND_2 = RegisterSoundEvent("horde_start_sound_2");

    public static void RegisterSounds(){
        Hotwaves.LOGGER.info("Registering sounds for {}", Hotwaves.MOD_ID);
    }

    private static SoundEvent RegisterSoundEvent(String name){
        Identifier id = new Identifier(Hotwaves.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
