package com.aric3435.temporaltrials.player;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;

/**
 * MultiplayerLivesManager: Manages extra lives for multiplayer servers
 * 
 * Purpose:
 * - Prevents cycle resets from spam/trolling
 * - Makes progression possible with multiple players
 * - Adds challenge and teamwork elements
 * - Can be disabled via config
 * 
 * How it works:
 * - 1 player: 0 extra lives (pure survival)
 * - 2 players: 1 extra life per player (2 total chances)
 * - 3 players: 2 extra lives per player (6 total chances)
 * - 4+ players: 3 extra lives per player (12+ chances)
 * 
 * Lives are reset every cycle for fair attempts
 */
public final class MultiplayerLivesManager {
    
    private static final Map<ServerWorld, MultiplayerLivesManager> MANAGERS = new WeakHashMap<>();
    
    private final ServerWorld world;
    private final Map<UUID, Integer> playerLives = new HashMap<>();
    
    private MultiplayerLivesManager(ServerWorld world) {
        this.world = world;
        updateLivesForAllPlayers();
    }
    
    /**
     * Get or create lives manager for a world
     */
    public static MultiplayerLivesManager get(ServerWorld world) {
        return MANAGERS.computeIfAbsent(world, MultiplayerLivesManager::new);
    }
    
    /**
     * Calculate extra lives based on player count
     * 
     * Formula:
     * - 1 player: 0 lives
     * - 2 players: 1 life each
     * - 3 players: 2 lives each
     * - 4+ players: 3 lives each
     * 
     * This scales with server size
     */
    private int calculateExtraLives(int playerCount) {
        // If disabled, everyone gets 0 lives
        if (!TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED) {
            return 0;
        }
        
        if (playerCount <= 1) return 0;
        if (playerCount == 2) return 1;
        if (playerCount == 3) return 2;
        return 3; // 4+ players
    }
    
    /**
     * Get current player count in this world
     */
    public int getPlayerCount() {
        return world.getPlayers().size();
    }
    
    /**
     * Update lives for all players based on current player count
     * Called when cycle resets or players join/leave
     */
    public void updateLivesForAllPlayers() {
        int playerCount = getPlayerCount();
        int extraLives = calculateExtraLives(playerCount);
        
        System.out.println("[TemporalTrials] Updating multiplayer lives for " + playerCount + 
                         " players (" + (extraLives > 0 ? "+" : "") + extraLives + " lives each)");
        
        playerLives.clear();
        for (ServerPlayerEntity player : world.getPlayers()) {
            playerLives.put(player.getUuid(), extraLives);
            
            if (extraLives > 0) {
                String displayLives = "§6Lives: ";
                for (int i = 0; i < extraLives; i++) {
                    displayLives += "§c❤ ";
                }
                player.sendMessage(Text.of(displayLives), false);
            } else {
                player.sendMessage(Text.of("§6No extra lives - One wrong move and the cycle fails!"), false);
            }
        }
    }
    
    /**
     * Get remaining lives for a player
     */
    public int getPlayerLives(UUID playerUuid) {
        return playerLives.getOrDefault(playerUuid, 0);
    }
    
    /**
     * Consume one life from a player
     * Returns remaining lives after consumption
     */
    public int consumeLife(UUID playerUuid) {
        int current = playerLives.getOrDefault(playerUuid, 0);
        int remaining = Math.max(0, current - 1);
        playerLives.put(playerUuid, remaining);
        
        System.out.println("[TemporalTrials] Player " + playerUuid + " consumed a life. Remaining: " + remaining);
        
        return remaining;
    }
    
    /**
     * Reset all lives (called when cycle resets via Flute)
     */
    public void resetAllLives() {
        System.out.println("[TemporalTrials] Resetting all player lives for new cycle");
        updateLivesForAllPlayers();
    }
    
    /**
     * Debug: Set lives for a player
     */
    public void setPlayerLives(UUID playerUuid, int lives) {
        playerLives.put(playerUuid, Math.max(0, lives));
        System.out.println("[TemporalTrials] Set player " + playerUuid + " to " + lives + " lives");
    }
    
    /**
     * Debug: Get lives info for all players
     */
    public void printDebugInfo() {
        System.out.println("[TemporalTrials] === Lives Debug Info ===");
        System.out.println("[TemporalTrials] Total players: " + getPlayerCount());
        System.out.println("[TemporalTrials] Lives per player: " + calculateExtraLives(getPlayerCount()));
        for (Map.Entry<UUID, Integer> entry : playerLives.entrySet()) {
            System.out.println("[TemporalTrials] " + entry.getKey() + " = " + entry.getValue() + " lives");
        }
    }
}