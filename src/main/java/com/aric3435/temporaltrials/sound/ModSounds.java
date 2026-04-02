package com.aric3435.temporaltrials.sound;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    private ModSounds() {}

    // SoundEvent instances (for playback)
    public static SoundEvent DAY_1;
    public static SoundEvent DAY_2;
    public static SoundEvent DAY_3;

    // RegistryKey<JukeboxSong> values (used by Item.Settings().jukeboxPlayable)
    public static final RegistryKey<JukeboxSong> DAY_1_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_1"));
    public static final RegistryKey<JukeboxSong> DAY_2_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_2"));
    public static final RegistryKey<JukeboxSong> DAY_3_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_3"));

    public static void register() {
        // Register SoundEvents only. Do NOT attempt to construct JukeboxSong here — mappings differ.
        DAY_1 = registerSound("day_1");
        DAY_2 = registerSound("day_2");
        DAY_3 = registerSound("day_3");
    }

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of(TemporalTrialsMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
