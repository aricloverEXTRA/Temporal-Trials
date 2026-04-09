package com.aric3435.temporaltrials.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.WeakHashMap;
import java.util.Map;

/**
 * PlayerDataProvider: Manages persistent player data across world resets.
 * 
 * Uses WeakHashMap so data is automatically cleaned up when player disconnects.
 * 
 * The saved inventory persists across resets:
 * - Easy/Normal: Acts as safety net (you get it back if you die)
 * - Hard Mode: Acts as progression tracker (dying loses it, Flute saves it)
 */
public class PlayerDataProvider {

    // Persistent storage using WeakHashMap
    // This survives world resets because the player entity persists
    private static final Map<ServerPlayerEntity, PlayerDataComponent> PLAYER_DATA = new WeakHashMap<>();

    /**
     * Get or create player data component
     */
    public static PlayerDataComponent get(ServerPlayerEntity player) {
        return PLAYER_DATA.computeIfAbsent(player, p -> {
            System.out.println("[TemporalTrials] Created new player data for " + p.getName().getString());
            return new PlayerDataComponent();
        });
    }

    /**
     * Register player event handlers
     */
    public static void register() {
        // When player respawns (dies and respawns)
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            PlayerDataComponent oldData = PLAYER_DATA.get(oldPlayer);
            
            if (oldData != null && alive) {
                // Player respawned - copy their old data to new player entity
                PlayerDataComponent newData = new PlayerDataComponent();
                newData.saveInventory(oldData.getSavedInventory());
                PLAYER_DATA.put(newPlayer, newData);
                
                System.out.println("[TemporalTrials] Copied player data on respawn for " + newPlayer.getName().getString());
            } else {
                // Player died - don't carry over data (handled by death logic elsewhere)
                System.out.println("[TemporalTrials] Player " + newPlayer.getName().getString() + " respawned without data copy");
            }
        });

        // Clean up old player data when they leave
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PLAYER_DATA.remove(oldPlayer);
        });

        System.out.println("[TemporalTrials] PlayerDataProvider registered");
    }
}