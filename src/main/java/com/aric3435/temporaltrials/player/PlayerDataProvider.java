package com.aric3435.temporaltrials.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerDataProvider: Manages persistent player data for Temporal Trials.
 * Stores saved inventory and per-player lives for the current cycle.
 */
public class PlayerDataProvider {
    // Holds all player data keyed by UUID to avoid issues with entity identity (disconnection, death)
    private static final Map<UUID, PlayerDataComponent> PLAYER_DATA = new HashMap<>();

    /**
     * Get or create player data for a ServerPlayerEntity.
     */
    public static PlayerDataComponent get(ServerPlayerEntity player) {
        return PLAYER_DATA.computeIfAbsent(player.getUuid(), uuid -> {
            System.out.println("[TemporalTrials] Created new player data for " + player.getName().getString());
            return new PlayerDataComponent();
        });
    }

    /**
     * Set remaining lives for a player.
     */
    public static void setRemainingLives(ServerPlayerEntity player, int lives) {
        get(player).setRemainingLives(lives);
    }

    /**
     * Get remaining lives for a player.
     */
    public static int getRemainingLives(ServerPlayerEntity player) {
        return get(player).getRemainingLives();
    }

    /**
     * Register player event handlers for cleanup.
     */
    public static void register() {
        // When the player leaves, clean up.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PLAYER_DATA.remove(oldPlayer.getUuid());
        });
        // (Optional): clear on player disconnect if needed – not strictly required due to short-lived nature.
        System.out.println("[TemporalTrials] PlayerDataProvider: Registered for cleanup.");
    }

    /**
     * For testing/admin: clear all data.
     */
    public static void clearAll() {
        PLAYER_DATA.clear();
    }
}