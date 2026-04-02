package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.sound.ModSounds;
import com.aric3435.temporaltrials.world.LoopManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

public final class MusicController {
    private static PositionedSoundInstance currentMusic = null;
    private static SoundEvent currentTrack = null;

    private MusicController() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MusicController::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            stopMusic(client);
            return;
        }

        if (!LoopManager.isTemporalTrials(client.world)) {
            stopMusic(client);
            return;
        }

        if (!LoopStateClientState.active) {
            stopMusic(client);
            return;
        }

        long time = client.world.getTimeOfDay() % 24000;
        boolean isNight = time >= 13000;

        if (isNight) {
            stopMusic(client);
            return;
        }

        SoundEvent desired = switch (LoopStateClientState.day) {
            case 1 -> ModSounds.DAY_1;
            case 2 -> ModSounds.DAY_2;
            default -> ModSounds.DAY_3;
        };

        // If the desired track is already playing, do nothing
        if (currentTrack == desired && currentMusic != null) return;

        // Otherwise stop previous and play new
        stopMusic(client);

        if (desired != null) {
            currentMusic = PositionedSoundInstance.music(desired);
            currentTrack = desired;
            client.getSoundManager().play(currentMusic);
        }
    }

    private static void stopMusic(MinecraftClient client) {
        if (currentMusic != null) {
            client.getSoundManager().stop(currentMusic);
            currentMusic = null;
            currentTrack = null;
        }
    }
}
