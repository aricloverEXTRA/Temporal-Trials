package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.sound.ModSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

/**
 * ClientMusicController: plays ambient music client-side based on loop state.
 */
public final class ClientMusicController {
    private static SoundEvent currentTrack = null;
    private static PositionedSoundInstance playingInstance = null;

    private ClientMusicController() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientMusicController::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            stop(client);
            return;
        }

        if (!TemporalTrialsClientConfig.BACKGROUND_MUSIC_ENABLED) {
            stop(client);
            return;
        }

        if (!ClientLoopState.isActive()) {
            stop(client);
            return;
        }

        int day = ClientLoopState.getDay();
        int trackIndex = TemporalTrialsConfig.getMusicTrackForDay(day);
        SoundEvent desired = switch (trackIndex) {
            case 1 -> ModSounds.DAY_1;
            case 2 -> ModSounds.DAY_2;
            default -> ModSounds.DAY_3;
        };

        if (desired == null) {
            stop(client);
            return;
        }

        if (currentTrack == desired && playingInstance != null) return;

        stop(client);

        playingInstance = PositionedSoundInstance.music(desired);
        currentTrack = desired;
        client.getSoundManager().play(playingInstance);
    }

    private static void stop(MinecraftClient client) {
        if (playingInstance != null) {
            client.getSoundManager().stop(playingInstance);
            playingInstance = null;
            currentTrack = null;
        }
    }
}