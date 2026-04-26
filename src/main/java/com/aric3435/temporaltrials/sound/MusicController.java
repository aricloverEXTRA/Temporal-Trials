package com.aric3435.temporaltrials.sound;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;

public final class MusicController {
    private MusicController() {}

    private static int lastPlayedDay = -1;

    public static void tick(MinecraftServer server, int currentDay, boolean force) {
        if (!TemporalTrialsConfig.BACKGROUND_MUSIC_ENABLED) return;
        if (!force && lastPlayedDay == currentDay) return;

        SoundEvent event = getSoundForDay(currentDay);
        if (event == null) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.playSound(event, 1.0f, 1.0f);
        }

        lastPlayedDay = currentDay;
        System.out.println("[TemporalTrials] Played music for day " + currentDay);
    }

    public static void reset() {
        lastPlayedDay = -1;
    }

    private static SoundEvent getSoundForDay(int day) {
        int track = com.aric3435.temporaltrials.config.TemporalTrialsConfig.getMusicTrackForDay(day);
        return switch (track) {
            case 1 -> ModSounds.DAY_1;
            case 2 -> ModSounds.DAY_2;
            case 3 -> ModSounds.DAY_3;
            default -> null;
        };
    }
}