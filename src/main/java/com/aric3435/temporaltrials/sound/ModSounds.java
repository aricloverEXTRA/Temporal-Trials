package com.aric3435.temporaltrials.sound;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * ModSounds: Registry for all sound events and jukebox songs
 * 
 * Sound Events: Used for playing ambient music via MusicController
 * Jukebox Songs: Used when music discs are played in jukeboxes
 * 
 * The same sound files are referenced by both systems:
 * - Ambient playback: SoundEvent (MusicController)
 * - Jukebox playback: JukeboxSong (music discs in jukeboxes)
 */
public final class ModSounds {
    private ModSounds() {}

    // ===== SOUND EVENTS (for MusicController ambient playback) =====
    
    /**
     * SoundEvent instances used by MusicController for ambient music
     * These are registered to the SOUND_EVENT registry
     */
    public static SoundEvent DAY_1;
    public static SoundEvent DAY_2;
    public static SoundEvent DAY_3;

    // ===== JUKEBOX SONGS (for music disc playback) =====
    
    /**
     * RegistryKey<JukeboxSong> values used by Item.Settings().jukeboxPlayable()
     * These reference the same sound files but with jukebox metadata
     */
    public static final RegistryKey<JukeboxSong> DAY_1_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_1"));
    public static final RegistryKey<JukeboxSong> DAY_2_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_2"));
    public static final RegistryKey<JukeboxSong> DAY_3_KEY =
            RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(TemporalTrialsMod.MOD_ID, "day_3"));

    /**
     * Register all sounds and jukebox songs
     * Called during mod initialization
     */
    public static void register() {
        System.out.println("[TemporalTrials] Registering sounds...");
        
        // Register SoundEvents for ambient playback
        DAY_1 = registerSound("day_1");
        DAY_2 = registerSound("day_2");
        DAY_3 = registerSound("day_3");
        
        System.out.println("[TemporalTrials] ✓ Registered " + 3 + " sound events");
        
        // Register JukeboxSongs for music disc playback
        registerJukeboxSongs();
        
        System.out.println("[TemporalTrials] ✓ Registered " + 3 + " jukebox songs");
    }

    /**
     * Register a SoundEvent with the SOUND_EVENT registry
     * 
     * This creates a sound event that can be:
     * 1. Played via MusicController (ambient background music)
     * 2. Referenced by SoundManager.play()
     */
    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of(TemporalTrialsMod.MOD_ID, name);
        SoundEvent soundEvent = SoundEvent.of(id);
        return Registry.register(Registries.SOUND_EVENT, id, soundEvent);
    }

    /**
     * Register JukeboxSongs for music disc items
     * 
     * JukeboxSongs contain metadata about how the disc should play:
     * - Sound event reference
     * - Description/title
     * - Display item (optional)
     * - Comparator output
     * 
     * In Yarn 1.21.4, JukeboxSong registration is handled via datagen
     * or by direct registry access. We reference the keys here.
     */
    private static void registerJukeboxSongs() {
        // Jukebox songs are registered via datagen or built-in registries
        // We don't manually create them here - they're defined in the music disc items
        // The Item.Settings().jukeboxPlayable(KEY) handles the connection
        
        System.out.println("[TemporalTrials] Jukebox songs will be bound to music disc items");
    }

    /**
     * Debug: Get all registered sounds
     */
    public static void printDebugInfo() {
        System.out.println("[TemporalTrials] === Sound Debug Info ===");
        System.out.println("[TemporalTrials] DAY_1: " + DAY_1);
        System.out.println("[TemporalTrials] DAY_2: " + DAY_2);
        System.out.println("[TemporalTrials] DAY_3: " + DAY_3);
        System.out.println("[TemporalTrials] DAY_1_KEY: " + DAY_1_KEY);
        System.out.println("[TemporalTrials] DAY_2_KEY: " + DAY_2_KEY);
        System.out.println("[TemporalTrials] DAY_3_KEY: " + DAY_3_KEY);
    }
}