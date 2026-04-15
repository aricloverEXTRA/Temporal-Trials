package com.aric3435.temporaltrials.player;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.world.LoopManager;
import com.aric3435.temporaltrials.world.WorldStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameMode;
import net.minecraft.item.ItemStack;

/**
 * PlayerDeathHandler: Handles death logic in Temporal Trials
 * 
 * Death mechanics:
 * - Check multiplayer lives system
 * - If lives available: Respawn at spawn with inventory penalty (based on difficulty)
 * - If no lives: Trigger world reset/cycle fail OR move to spectator (if multiplayer protection enabled)
 * 
 * Difficulty specific:
 * - Easy/Normal: Death with lives = respawn with saved inventory
 * - Hard Mode: Death with lives = respawn with EMPTY inventory (lose all items)
 * 
 * Multiplayer Protection (PREVENT_SINGLE_DEATH_RESET):
 * - When enabled: Dead player moves to spectator mode
 * - Only resets cycle when ALL players are dead
 * - Allows remaining players to continue
 * 
 * Player rejoin:
 * - When player rejoins, restore original world chunks
 * - Prevents persistent world changes
 */
public final class PlayerDeathHandler {
    private PlayerDeathHandler() {}

    public static void register() {
        // Register death event handler
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
                return true;
            }

            if (!LoopManager.isTemporalTrials(serverWorld)) {
                return true;
            }

            // Handle Temporal Trials death
            handleTemporalTrialsDeath(serverWorld, player);
            return false; // Cancel death - we handle the reset
        });

        // Register player rejoin event
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.getWorld() instanceof ServerWorld serverWorld) {
                if (LoopManager.isTemporalTrials(serverWorld)) {
                    // Player rejoined - restore world state
                    WorldStateManager.restoreOriginalChunks(serverWorld);
                    System.out.println("[TemporalTrials] Player " + newPlayer.getName().getString() + 
                                     " rejoined - restoring world state");
                }
            }
        });

        System.out.println("[TemporalTrials] Death handler registered");
    }

    /**
     * Handles player death in Temporal Trials
     * 
     * Flow:
     * 1. Check if multiplayer (2+ players)
     * 2. If multiplayer + protection enabled: Move to spectator instead of reset
     * 3. If singleplayer or no protection: Use lives system
     */
    private static void handleTemporalTrialsDeath(ServerWorld world, ServerPlayerEntity player) {
        System.out.println("[TemporalTrials] ✗ Player " + player.getName().getString() + " died!");
        
        int playerCount = world.getPlayers().size();
        boolean isMultiplayer = playerCount >= 2;
        int difficulty = world.getDifficulty().getId();
        boolean isHardMode = difficulty >= 3; // 3 = Hard
        
        // MULTIPLAYER PROTECTION: Convert to spectator instead of cycle reset
        if (isMultiplayer && TemporalTrialsConfig.PREVENT_SINGLE_DEATH_RESET) {
            handleMultiplayerDeath(world, player, isHardMode);
            return;
        }
        
        // STANDARD DEATH: Check lives system
        MultiplayerLivesManager livesMgr = null;
        int remainingLives = 0;
        
        if (TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED) {
            livesMgr = MultiplayerLivesManager.get(world);
            remainingLives = livesMgr.getPlayerLives(player.getUuid());
        }
        
        if (remainingLives > 0) {
            handlePlayerRespawnWithLives(world, player, isHardMode, remainingLives, livesMgr);
        } else {
            handlePlayerDeathWithoutLives(world, player, isHardMode);
        }
    }

    /**
     * MULTIPLAYER PROTECTION MODE
     * 
     * Player dies → Move to spectator mode
     * Allows other players to continue and try to win
     * Only resets cycle if ALL players are dead
     */
    private static void handleMultiplayerDeath(ServerWorld world, ServerPlayerEntity player, boolean isHardMode) {
        System.out.println("[TemporalTrials] MULTIPLAYER MODE: Player moved to spectator");
        
        // Move player to spectator mode
        player.changeGameMode(GameMode.SPECTATOR);
        
        player.sendMessage(
                Text.of("§c§l✗ You have fallen..."),
                false
        );
        player.sendMessage(
                Text.of("§6You are now in spectator mode."),
                false
        );
        player.sendMessage(
                Text.of("§6Your teammates can continue trying to win!"),
                false
        );
        
        // Announce to all players
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (!p.equals(player)) {
                p.sendMessage(
                        Text.of("§e⚠ " + player.getName().getString() + " has fallen to spectator mode!"),
                        false
                );
            }
        }
        
        // Check if ALL players are dead (in spectator mode)
        boolean allPlayersDead = world.getPlayers().stream()
                .allMatch(p -> p.isSpectator());
        
        if (allPlayersDead) {
            // All players are dead - trigger cycle fail
            System.out.println("[TemporalTrials] All players eliminated - triggering cycle fail");
            
            for (ServerPlayerEntity p : world.getPlayers()) {
                p.sendMessage(
                        Text.of("§4§l✗✗ All players have fallen!"),
                        false
                );
                p.sendMessage(
                        Text.of("§4§lThe cycle has FAILED."),
                        false
                );
            }
            
            LoopManager.handleCycleFail(world);
        }
    }

    /**
     * Player respawns at spawn with lives remaining
     * 
     * Easy/Normal: Keeps saved inventory (protected by Flute)
     * Hard Mode: Loses all inventory (risk/reward)
     */
    private static void handlePlayerRespawnWithLives(ServerWorld world, ServerPlayerEntity player, 
                                                      boolean isHardMode, int remainingLives,
                                                      MultiplayerLivesManager livesMgr) {
        // Consume one life
        int livesAfter = livesMgr.consumeLife(player.getUuid());
        
        System.out.println("[TemporalTrials] Player " + player.getName().getString() + 
                         " respawning with " + livesAfter + " lives remaining");
        
        // Teleport to spawn
        var spawn = world.getSpawnPos();
        player.requestTeleport(
                spawn.getX() + 0.5,
                spawn.getY() + 1,
                spawn.getZ() + 0.5
        );
        
        // Restore health
        player.setHealth(player.getMaxHealth());
        
        // Handle inventory based on difficulty
        if (isHardMode) {
            // Hard Mode: Lose all inventory on death
            player.getInventory().clear();
            player.sendMessage(
                    Text.of("§4§l✗ You died in HARD MODE!"),
                    false
            );
            player.sendMessage(
                    Text.of("§c§lAll your items are lost."),
                    false
            );
            player.sendMessage(
                    Text.of("§e" + livesAfter + " lives remaining."),
                    false
            );
        } else {
            // Easy/Normal: Keep saved inventory (protected by Flute)
            PlayerDataComponent playerData = PlayerDataProvider.get(player);
            DefaultedList<ItemStack> savedInventory = playerData.getSavedInventory();
            
            player.getInventory().clear();
            for (int i = 0; i < Math.min(savedInventory.size(), player.getInventory().main.size()); i++) {
                player.getInventory().main.set(i, savedInventory.get(i).copy());
            }
            
            player.sendMessage(
                    Text.of("§e§l✓ You died, but your Flute protected your inventory."),
                    false
            );
            player.sendMessage(
                    Text.of("§e" + livesAfter + " lives remaining."),
                    false
            );
        }
        
        // Show remaining lives to all players
        String lifeDisplay = "§6Lives Remaining: ";
        for (int i = 0; i < livesAfter; i++) {
            lifeDisplay += "§c❤ ";
        }
        
        for (ServerPlayerEntity p : world.getPlayers()) {
            p.sendMessage(Text.of(lifeDisplay), false);
        }
    }

    /**
     * Player has no lives - world reset happens (cycle fails)
     * But inventory is restored from last Flute use
     */
    private static void handlePlayerDeathWithoutLives(ServerWorld world, ServerPlayerEntity player, 
                                                       boolean isHardMode) {
        System.out.println("[TemporalTrials] ✗✗ Player " + player.getName().getString() + 
                         " has no lives left - triggering world reset");
        
        // Announce to all players
        for (ServerPlayerEntity p : world.getPlayers()) {
            p.sendMessage(
                    Text.of("§4§l✗ " + player.getName().getString() + " has fallen..."),
                    false
            );
            p.sendMessage(
                    Text.of("§4§lThe cycle has FAILED."),
                    false
            );
        }
        
        // Trigger world reset via LoopManager
        LoopManager.handleCycleFail(world);
    }
}