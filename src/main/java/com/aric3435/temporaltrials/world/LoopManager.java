package com.aric3435.temporaltrials.world;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.player.PlayerDataComponent;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import com.aric3435.temporaltrials.player.MultiplayerLivesManager;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.sound.MusicController;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LoopManager {
    private LoopManager() {}

    private static long loopStartTick = -1L;
    private static boolean loopActive = false;

    private static final boolean DEBUG_COMMANDS_ENABLED = true;
    private static final Map<ServerWorld, Boolean> TEST_ENABLED = new WeakHashMap<>();

    private static final AtomicBoolean isResetting = new AtomicBoolean(false);
    private static final Map<ServerWorld, Long> lastResetTime = new WeakHashMap<>();

    private static final Identifier LOOP_STATE_CHANNEL = Identifier.of(TemporalTrialsMod.MOD_ID, "loop_state");

    public static void setEnabledForWorld(ServerWorld world, boolean enabled) {
        if (!DEBUG_COMMANDS_ENABLED) return;
        if (world == null) return;
        synchronized (TEST_ENABLED) { TEST_ENABLED.put(world, enabled); }
        System.out.println("[TemporalTrials] Debug override set for world " + world.getRegistryKey() + " -> " + enabled);
    }

    public static boolean toggleForWorld(ServerWorld world) {
        if (!DEBUG_COMMANDS_ENABLED) return false;
        if (world == null) return false;
        boolean newVal;
        synchronized (TEST_ENABLED) {
            boolean current = TEST_ENABLED.containsKey(world) ? TEST_ENABLED.get(world) : isTemporalTrials(world);
            newVal = !current;
            TEST_ENABLED.put(world, newVal);
        }
        System.out.println("[TemporalTrials] Debug override toggled for world " + world.getRegistryKey() + " -> " + newVal);
        return newVal;
    }

    public static boolean clearOverrideForWorld(ServerWorld world) {
        if (!DEBUG_COMMANDS_ENABLED) return false;
        if (world == null) return false;
        synchronized (TEST_ENABLED) {
            boolean removed = TEST_ENABLED.remove(world) != null;
            if (removed) System.out.println("[TemporalTrials] Debug override cleared for world " + world.getRegistryKey());
            return removed;
        }
    }

    public static boolean isTemporalTrials(World world) {
        if (world instanceof ServerWorld sw) {
            synchronized (TEST_ENABLED) {
                if (TEST_ENABLED.containsKey(sw)) {
                    Boolean override = TEST_ENABLED.get(sw);
                    if (override != null) return override;
                }
            }
        }
        try {
            return world.getRegistryKey().equals(TemporalTrialsMod.TEMPORAL_TRIALS_DIMENSION);
        } catch (Exception e) {
            return false;
        }
    }

    public static void forceStartLoop(ServerWorld world) {
        if (world == null) return;
        loopStartTick = world.getTimeOfDay();
        loopActive = true;
        System.out.println("[TemporalTrials] Loop forced start at tick " + loopStartTick);
        sendStateToAll(world, true, 1, TemporalTrialsConfig.getLoopDurationTicks());
        MusicController.tick(world.getServer(), 1, true);
    }

    public static void forceStopLoop(ServerWorld world) {
        if (world == null) return;
        loopActive = false;
        System.out.println("[TemporalTrials] Loop forced stop");
        sendStateToAll(world, false, 1, TemporalTrialsConfig.getLoopDurationTicks());
        MusicController.reset();
    }

    public static int getCurrentDayForWorld(ServerWorld world) {
        if (world == null) return 1;
        long now = world.getTimeOfDay();
        long elapsed = (loopStartTick < 0) ? 0 : now - loopStartTick;
        return getCurrentDayFromElapsed(elapsed);
    }

    public static void startLoop(ServerWorld world) {
        if (!isTemporalTrials(world)) return;
        loopStartTick = world.getTimeOfDay();
        loopActive = true;

        for (ServerPlayerEntity player : world.getPlayers()) {
            String dayName = TemporalTrialsConfig.getDayName(1);
            player.sendMessage(Text.of("§6§l✦ Dawn of the " + dayName + "... ✦"), false);
        }

        sendStateToAll(world, true, 1, TemporalTrialsConfig.getLoopDurationTicks());
        MusicController.tick(world.getServer(), 1, true);
    }

    public static void tick(ServerWorld world) {
        if (!isTemporalTrials(world)) return;
        if (!loopActive) {
            startLoop(world);
            return;
        }

        long now = world.getTimeOfDay();
        long elapsed = now - loopStartTick;
        if (elapsed < 0) { loopStartTick = now; elapsed = 0; }

        int currentDay = getCurrentDayFromElapsed(elapsed);
        long remaining = Math.max(0L, TemporalTrialsConfig.getLoopDurationTicks() - elapsed);

        sendStateToAll(world, false, currentDay, remaining);
        handleDayTransitions(world, currentDay, elapsed);

        PaleGardenCorruption.tick(world, currentDay);

        if (elapsed >= TemporalTrialsConfig.getLoopDurationTicks()) {
            handleLoopEnd(world);
        }
    }

    private static int getCurrentDayFromElapsed(long elapsed) {
        long dayLength = TemporalTrialsConfig.getDayLengthTicks();
        int day = (int) (elapsed / dayLength) + 1;
        int maxDays = TemporalTrialsConfig.CYCLE_LENGTH_DAYS;
        if (day > maxDays) day = maxDays;
        return day;
    }

    private static void handleDayTransitions(ServerWorld world, int currentDay, long elapsed) {
        if (elapsed == 0) return;

        long dayLength = TemporalTrialsConfig.getDayLengthTicks();
        int maxDays = TemporalTrialsConfig.CYCLE_LENGTH_DAYS;

        for (int day = 1; day <= maxDays; day++) {
            long transitionTick = (long)(day - 1) * dayLength;
            if (elapsed >= transitionTick && elapsed < transitionTick + 2) {
                String dayName = TemporalTrialsConfig.getDayName(day);
                for (ServerPlayerEntity player : world.getPlayers()) {
                    if (day >= maxDays) player.sendMessage(Text.of("§4§l✦ " + dayName + " ✦"), false);
                    else if (day == 2) player.sendMessage(Text.of("§e§l✦ " + dayName + " ✦"), false);
                    else player.sendMessage(Text.of("§6§l✦ " + dayName + " ✦"), false);
                }
                MusicController.tick(world.getServer(), day, false);
                return;
            }
        }
    }

    private static void handleLoopEnd(ServerWorld world) {
        System.out.println("[TemporalTrials] === LOOP END: Cycle time limit reached ===");
        boolean averted = checkWinCondition(world);
        if (!averted) triggerMoonfall(world);
    }

    private static boolean checkWinCondition(ServerWorld world) {
        return false;
    }

    private static void triggerMoonfall(ServerWorld world) {
        if (isResetting.getAndSet(true)) {
            System.out.println("[TemporalTrials] ⚠ Moon crash already in progress, skipping");
            return;
        }

        Long lastReset = lastResetTime.get(world);
        long currentTime = System.currentTimeMillis();
        if (lastReset != null && currentTime - lastReset < 10000) {
            System.out.println("[TemporalTrials] ⚠ Moon crash on cooldown, skipping");
            isResetting.set(false);
            return;
        }

        System.out.println("[TemporalTrials] ⚠ The moon is descending!");
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(Text.of("§4§l╔════════════════════════════════════╗"), false);
            player.sendMessage(Text.of("§4§l║      ⚠ THE MOON DESCENDS ⚠        ║"), false);
            player.sendMessage(Text.of("§4§l║   You failed to escape in time...   ║"), false);
            player.sendMessage(Text.of("§4§l║    The world resets in 5 seconds... ║"), false);
            player.sendMessage(Text.of("§4§l╚════════════════════════════════════╝"), false);

            player.playSound(net.minecraft.sound.SoundEvents.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
        }

        world.getServer().submit(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            handleCycleFail(world);
            isResetting.set(false);
        });
    }

    public static void resetLoop(ServerWorld world) {
        if (world == null) return;
        if (!isResetting.compareAndSet(false, true)) {
            System.out.println("[TemporalTrials] ⚠ Reset already in progress, skipping");
            return;
        }

        try {
            System.out.println("[TemporalTrials] Starting world reset to Day 1");
            loopActive = false;
            world.setTimeOfDay(0L);

            BlockPos spawn = world.getSpawnPos();
            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerDataComponent playerData = PlayerDataProvider.get(player);
                DefaultedList<ItemStack> savedInventory = playerData.getSavedInventory();

                player.getInventory().clear();
                for (int i = 0; i < Math.min(savedInventory.size(), player.getInventory().main.size()); i++) {
                    player.getInventory().main.set(i, savedInventory.get(i).copy());
                }

                player.setHealth(player.getMaxHealth());
                player.setExperienceLevel(0);
                player.addExperience(-player.totalExperience);

                player.requestTeleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
            }

            startProgressiveChunkRegeneration(world, spawn);

            if (TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED) {
                MultiplayerLivesManager livesMgr = MultiplayerLivesManager.get(world);
                livesMgr.resetAllLives();
            }

            loopStartTick = world.getTimeOfDay();
            loopActive = true;
            lastResetTime.put(world, System.currentTimeMillis());

            for (ServerPlayerEntity player : world.getPlayers()) {
                player.sendMessage(Text.of("§b§l╔════════════════════════════════════╗"), false);
                player.sendMessage(Text.of("§b§l║   World reset to Day 1 complete!   ║"), false);
                player.sendMessage(Text.of("§b§l║    Your progress is preserved.      ║"), false);
                player.sendMessage(Text.of("§b§l║  Chunks regenerating in background. ║"), false);
                player.sendMessage(Text.of("§b§l╚════════════════════════════════════╝"), false);
            }

            MusicController.reset();
            sendStateToAll(world, true, 1, TemporalTrialsConfig.getLoopDurationTicks());

            System.out.println("[TemporalTrials] World reset complete");
        } finally {
            isResetting.set(false);
        }
    }

    public static void handleCycleFail(ServerWorld world) {
        if (world == null) return;

        loopActive = false;
        world.setTimeOfDay(0L);
        BlockPos spawn = world.getSpawnPos();

        for (ServerPlayerEntity player : world.getPlayers()) {
            PlayerDataComponent playerData = PlayerDataProvider.get(player);
            DefaultedList<ItemStack> savedInventory = playerData.getSavedInventory();

            player.getInventory().clear();
            for (int i = 0; i < Math.min(savedInventory.size(), player.getInventory().main.size()); i++) {
                player.getInventory().main.set(i, savedInventory.get(i).copy());
            }

            player.setHealth(player.getMaxHealth());
            player.setExperienceLevel(0);
            player.addExperience(-player.totalExperience);

            player.requestTeleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
        }

        startProgressiveChunkRegeneration(world, spawn);

        if (TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED) {
            MultiplayerLivesManager livesMgr = MultiplayerLivesManager.get(world);
            livesMgr.resetAllLives();
        }

        loopStartTick = world.getTimeOfDay();
        loopActive = true;
        lastResetTime.put(world, System.currentTimeMillis());

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(Text.of("§4§l╔════════════════════════════════════╗"), false);
            player.sendMessage(Text.of("§4§l║      CYCLE FAILED                  ║"), false);
            player.sendMessage(Text.of("§4§l║ But your Flute protected your items.║"), false);
            player.sendMessage(Text.of("§4§l║      Try again on Day 1...          ║"), false);
            player.sendMessage(Text.of("§4§l╚════════════════════════════════════╝"), false);
        }

        MusicController.reset();
        sendStateToAll(world, true, 1, TemporalTrialsConfig.getLoopDurationTicks());
    }

    private static void startProgressiveChunkRegeneration(ServerWorld world, BlockPos center) {
        Thread asyncRegen = new Thread(() -> {
            try {
                int chunkRadius = TemporalTrialsConfig.CHUNK_REGEN_RADIUS;
                int centerChunkX = center.getX() >> 4;
                int centerChunkZ = center.getZ() >> 4;

                int totalChunks = (chunkRadius * 2 + 1) * (chunkRadius * 2 + 1);
                int processed = 0;

                for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                    for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                        int chunkX = centerChunkX + dx;
                        int chunkZ = centerChunkZ + dz;
                        try {
                            BlockPos chunkOrigin = new BlockPos(chunkX << 4, 0, chunkZ << 4);
                            world.getChunkManager().markForUpdate(chunkOrigin);
                        } catch (Exception e) {
                            System.err.println("[TemporalTrials] Error regenerating chunk " + chunkX + "," + chunkZ);
                        }
                        processed++;
                        if (processed % 100 == 0) System.out.println("[TemporalTrials] Chunk regen: " + processed + "/" + totalChunks);
                        if (processed % 10 == 0) Thread.sleep(5);
                    }
                }
            } catch (Exception e) {
                System.err.println("[TemporalTrials] Error during chunk regen: " + e.getMessage());
            }
        });

        asyncRegen.setName("TemporalTrials-ChunkRegen");
        asyncRegen.setDaemon(true);
        asyncRegen.start();
    }

    public static void manualRewind(ServerWorld world, ServerPlayerEntity player) {
        long current = world.getTimeOfDay();
        long newTime = current - (current % TemporalTrialsConfig.getDayLengthTicks());
        world.setTimeOfDay(newTime);
        BlockPos spawn = world.getSpawnPos();
        player.requestTeleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
    }

	private static void sendStateToAll(ServerWorld world, boolean showIntro, int day, long remainingTicks) {
		try {
			LoopStatePayload payload = new LoopStatePayload(loopActive, day, remainingTicks, showIntro);
			for (ServerPlayerEntity player : world.getPlayers()) {
				ServerPlayNetworking.send(player, payload);
			}
		} catch (Exception e) {
			System.err.println("[TemporalTrials] Failed to send loop state: " + e.getMessage());
			e.printStackTrace();
		}
	}
}