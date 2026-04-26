package com.aric3435.temporaltrials.world;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Random;

public class PaleGardenCorruption {
    private static final Random RANDOM = new Random();

    /**
     * Scales corruption attempts with cycle progress.
     * - Early days: almost none
     * - Final day: heavy spread
     */
    public static void tick(ServerWorld world, int currentDay) {
        if (!LoopManager.isTemporalTrials(world)) return;

        // Compute fraction of cycle completed (1.0 at final moment)
        double dayIndex = Math.max(1, Math.min(TemporalTrialsConfig.CYCLE_LENGTH_DAYS, currentDay));
        double fraction = (dayIndex - 1) / Math.max(1.0, (double) TemporalTrialsConfig.CYCLE_LENGTH_DAYS - 1.0);

        // Base attempts (max on final day)
        int baseMaxAttempts = 120;

        // Soft quadratic ramp so final day escalates quickly
        double scaled = Math.pow(fraction, 2.0);

        int attempts = (int) Math.round(baseMaxAttempts * scaled);

        // Ensure day 1 remains near zero
        if (currentDay <= 1) attempts = 0;

        // Spread attempts across players so server work scales with player count
        int players = Math.max(1, world.getPlayers().size());
        int attemptsPerPlayer = Math.max(0, attempts / players);

        if (attemptsPerPlayer <= 0) return;

        for (var player : world.getPlayers()) {
            BlockPos center = player.getBlockPos();

            for (int i = 0; i < attemptsPerPlayer; i++) {
                int dx = RANDOM.nextInt(TemporalTrialsConfig.CHUNK_REGEN_RADIUS) - TemporalTrialsConfig.CHUNK_REGEN_RADIUS/2;
                int dz = RANDOM.nextInt(TemporalTrialsConfig.CHUNK_REGEN_RADIUS) - TemporalTrialsConfig.CHUNK_REGEN_RADIUS/2;

                BlockPos surface = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, center.add(dx, 0, dz));

                // Only apply in plains and near surface
                if (!world.getBiome(surface).matchesKey(BiomeKeys.PLAINS)) continue;
                corruptBlock(world, surface);
            }
        }
    }

    private static void corruptBlock(ServerWorld world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == Blocks.GRASS_BLOCK) {
            world.setBlockState(pos, Blocks.PALE_MOSS_BLOCK.getDefaultState());
        } else if (block == Blocks.OAK_LOG) {
            world.setBlockState(pos, Blocks.PALE_OAK_LOG.getDefaultState());
        } else if (block == Blocks.OAK_LEAVES) {
            world.setBlockState(pos, Blocks.PALE_OAK_LEAVES.getDefaultState());
        } else if (block == Blocks.WATER && pos.getY() < world.getSeaLevel()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}