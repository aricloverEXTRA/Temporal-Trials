package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.sound.ModSounds;
import com.aric3435.temporaltrials.world.LoopManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.block.jukebox.JukeboxSong;

/**
 * MusicController: Manages background music for Temporal Trials
 * 
 * Features:
 * - Plays different tracks based on current day
 * - Respects day/night cycle (stops at night - 6pm to 6am)
 * - Adapts to configurable cycle length
 * - Music tracks stretch to fill longer cycles
 * - Uses Jukebox Song system for proper looping
 * 
 * Music Logic:
 * - DAY_1 music plays during early days (exploration phase)
 * - DAY_2 music plays during middle days (escalation phase)
 * - DAY_3 music always plays on final day (crisis phase)
 * 
 * Daytime Logic:
 * - 0-12,999 ticks (6am-6pm) = Daytime (music plays)
 * - 13,000-23,999 ticks (6pm-6am) = Nighttime (music stops)
 */
public final class MusicController {
    private static PositionedSoundInstance currentMusic = null;
    private static SoundEvent currentTrack = null;
    private static int lastDayPlayed = -1; // Track which day's music is playing

    private MusicController() {}

    /**
     * Register the music controller tick handler
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MusicController::tick);
        System.out.println("[TemporalTrials] Music controller registered");
    }

    /**
     * Main tick function - runs every client tick
     * Updates music based on current world state and day/night cycle
     */
    private static void tick(MinecraftClient client) {
        // Respect music config setting
        if (!TemporalTrialsConfig.BACKGROUND_MUSIC_ENABLED) {
            stopMusic(client);
            return;
        }

        // Check world and player validity
        if (client.world == null || client.player == null) {
            stopMusic(client);
            return;
        }

        // Only play in Temporal Trials world
        if (!LoopManager.isTemporalTrials(client.world)) {
            stopMusic(client);
            return;
        }

        // Only play when loop is active
        if (!LoopStateClientState.active) {
            stopMusic(client);
            return;
        }

        // ===== DAY/NIGHT CYCLE CHECK =====
        // Stop music during nighttime (6pm to 6am)
        // Minecraft time: 0 = 6am, 6000 = noon, 12000 = 6pm, 18000 = midnight, 24000 = 6am next day
        long time = client.world.getTimeOfDay() % 24000;
        
        // Night is from 13000 (6pm) to 23000 (before 6am)
        boolean isNight = time >= 13000;

        if (isNight) {
            stopMusic(client);
            return;
        }

        // ===== MUSIC TRACK SELECTION =====
        // Get appropriate music track for current day
        // This respects configurable cycle length
        int currentDay = LoopStateClientState.day;
        int musicTrack = TemporalTrialsConfig.getMusicTrackForDay(currentDay);

        // Get the SoundEvent for this track
        SoundEvent desired = switch (musicTrack) {
            case 1 -> {
                // Early exploration phase - calm
                yield ModSounds.DAY_1;
            }
            case 2 -> {
                // Middle escalation phase - tension
                yield ModSounds.DAY_2;
            }
            default -> {
                // Final crisis phase - always final day
                yield ModSounds.DAY_3;
            }
        };

        // Safety check - sound event should exist
        if (desired == null) {
            System.err.println("[TemporalTrials] ERROR: Sound event is null for day " + currentDay + " (track " + musicTrack + ")");
            stopMusic(client);
            return;
        }

        // ===== MUSIC PLAYBACK MANAGEMENT =====
        // Don't restart music if it's already playing
        if (currentTrack == desired && currentMusic != null && lastDayPlayed == musicTrack) {
            // Same track is playing, do nothing
            return;
        }

        // Different track or first time - start new music
        stopMusic(client);
        
        currentMusic = PositionedSoundInstance.music(desired);
        currentTrack = desired;
        lastDayPlayed = musicTrack;
        
        client.getSoundManager().play(currentMusic);
        
        System.out.println("[TemporalTrials] 🎵 Playing music for " + 
                         TemporalTrialsConfig.getDayName(currentDay) + 
                         " (Track " + musicTrack + ")");
    }

    /**
     * Stop currently playing music
     */
    private static void stopMusic(MinecraftClient client) {
        if (currentMusic != null) {
            client.getSoundManager().stop(currentMusic);
            currentMusic = null;
            currentTrack = null;
            lastDayPlayed = -1;
        }
    }

    /**
     * Debug: Get current playing track
     */
    public static SoundEvent getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Debug: Check if music is playing
     */
    public static boolean isPlaying() {
        return currentMusic != null && currentTrack != null;
    }
}