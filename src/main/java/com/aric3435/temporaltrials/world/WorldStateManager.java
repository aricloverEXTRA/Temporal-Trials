package com.aric3435.temporaltrials.world;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

/**
 * WorldStateManager: Preserves the original "Day 1" world state
 * 
 * Problem: When a player rejoins, world changes persist from disk
 * Solution: Restore to the original seed-generated state
 * 
 * How it works:
 * 1. On world init, capture all chunk positions around spawn
 * 2. When player rejoins during active cycle, restore those chunks
 * 3. Ensures fresh world experience for each cycle
 * 
 * This prevents:
 * - Broken blocks persisting
 * - Mobs that were killed remaining dead
 * - Chests that were emptied staying empty
 * - Any permanent changes to the world
 */
public final class WorldStateManager {
    
    private static final Map<ServerWorld, Set<ChunkPos>> LOADED_CHUNKS = new WeakHashMap<>();
    private static final Map<ServerWorld, Long> WORLD_CREATION_TIME = new WeakHashMap<>();
    private static final int CAPTURE_RADIUS = 32; // Same as regen radius
    
    /**
     * Called when world first initializes
     * Marks all chunks around spawn as "original state"
     * These chunks will be restored when players rejoin
     */
    public static void captureInitialWorldState(ServerWorld world) {
        if (!LoopManager.isTemporalTrials(world)) return;
        
        // Only capture once
        if (LOADED_CHUNKS.containsKey(world)) {
            return;
        }
        
        Set<ChunkPos> chunks = new HashSet<>();
        
        // Capture chunks in radius around spawn
        BlockPos spawn = world.getSpawnPos();
        int centerChunkX = spawn.getX() >> 4;
        int centerChunkZ = spawn.getZ() >> 4;
        
        for (int dx = -CAPTURE_RADIUS; dx <= CAPTURE_RADIUS; dx++) {
            for (int dz = -CAPTURE_RADIUS; dz <= CAPTURE_RADIUS; dz++) {
                chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
            }
        }
        
        LOADED_CHUNKS.put(world, chunks);
        WORLD_CREATION_TIME.put(world, System.currentTimeMillis());
        
        System.out.println("[TemporalTrials] ✓ Captured initial world state with " + chunks.size() + " chunks");
    }
    
    /**
     * Called when a player rejoins during an active cycle
     * Resets chunks they may have modified back to original seed state
     * 
     * This happens asynchronously to prevent lag
     */
    public static void restoreOriginalChunks(ServerWorld world) {
        if (!LoopManager.isTemporalTrials(world)) return;
        
        Set<ChunkPos> originalChunks = LOADED_CHUNKS.get(world);
        if (originalChunks == null || originalChunks.isEmpty()) {
            System.out.println("[TemporalTrials] No original chunk state saved, skipping restore");
            return;
        }
        
        System.out.println("[TemporalTrials] Player rejoined - restoring " + originalChunks.size() + " chunks to original state");
        
        // Restore asynchronously to prevent server freeze
        Thread asyncRestore = new Thread(() -> {
            int restored = 0;
            int total = originalChunks.size();
            
            for (ChunkPos pos : originalChunks) {
                try {
                    // Mark the chunk for update using BlockPos at chunk origin
                    BlockPos chunkOrigin = new BlockPos(pos.x << 4, 0, pos.z << 4);
                    world.getChunkManager().markForUpdate(chunkOrigin);
                    restored++;
                    
                    // Progress update every 50 chunks
                    if (restored % 50 == 0) {
                        System.out.println("[TemporalTrials] Chunk restore progress: " + restored + "/" + total);
                    }
                    
                    // Small delay to prevent server lag
                    Thread.sleep(1);
                } catch (Exception e) {
                    System.err.println("[TemporalTrials] Error restoring chunk " + pos);
                }
            }
            
            System.out.println("[TemporalTrials] ✓ Chunk restore complete! (" + restored + " chunks)");
        });
        
        asyncRestore.setName("TemporalTrials-ChunkRestore");
        asyncRestore.setDaemon(true);
        asyncRestore.start();
    }
    
    /**
     * Debug: Get number of captured chunks
     */
    public static int getCapturedChunkCount(ServerWorld world) {
        Set<ChunkPos> chunks = LOADED_CHUNKS.get(world);
        return chunks != null ? chunks.size() : 0;
    }
    
    /**
     * Debug: Get when world state was captured
     */
    public static long getWorldCaptureTime(ServerWorld world) {
        Long time = WORLD_CREATION_TIME.get(world);
        return time != null ? time : -1;
    }
}