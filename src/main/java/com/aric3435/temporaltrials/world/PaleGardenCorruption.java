package com.aric3435.temporaltrials.world;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Random;

public class PaleGardenCorruption {

    private static final Random RANDOM = new Random();

    public static void tick(ServerWorld world, int currentDay) {
        if (!LoopManager.isTemporalTrials(world)) return;

        int attempts = switch (currentDay) {
            case 1 -> 0;
            case 2 -> 20;
            default -> 60;
        };

        world.getPlayers().forEach(player -> {
            BlockPos center = player.getBlockPos();

            for (int i = 0; i < attempts; i++) {
                int dx = RANDOM.nextInt(16) - 8;
                int dz = RANDOM.nextInt(16) - 8;

                BlockPos surface = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, center.add(dx, 0, dz));

                if (!world.getBiome(surface).matchesKey(BiomeKeys.PLAINS)) continue;

                corruptBlock(world, surface);
            }
        });
    }

    private static void corruptBlock(ServerWorld world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == Blocks.GRASS_BLOCK) {
            world.setBlockState(pos, Blocks.PALE_MOSS_BLOCK.getDefaultState());
        }

        if (block == Blocks.OAK_LOG) {
            world.setBlockState(pos, Blocks.PALE_OAK_LOG.getDefaultState());
        }

        if (block == Blocks.OAK_LEAVES) {
            world.setBlockState(pos, Blocks.PALE_OAK_LEAVES.getDefaultState());
        }

        if (block == Blocks.WATER && pos.getY() < world.getSeaLevel()) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}
