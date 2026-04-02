package com.aric3435.temporaltrials.world;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.player.PlayerDataComponent;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * LoopManager: 3-day loop logic and debug helpers.
 * Uses PacketByteBuf + ServerPlayNetworking.send(player, Identifier, buf) for networking (Option A).
 */
public final class LoopManager {
    private LoopManager() {}

    private static final long LOOP_DURATION_TICKS = 72_000L;
    private static final long DAY_LENGTH_TICKS = 24_000L;

    private static long loopStartTick = -1L;
    private static boolean loopActive = false;

    // debug commands gate
    private static final boolean DEBUG_COMMANDS_ENABLED = true;
    private static final Map<ServerWorld, Boolean> TEST_ENABLED = new WeakHashMap<>();

    private static final Identifier LOOP_STATE_CHANNEL = Identifier.of(TemporalTrialsMod.MOD_ID, "loop_state");

    public static void setEnabledForWorld(ServerWorld world, boolean enabled) {
        if (!DEBUG_COMMANDS_ENABLED) return;
        if (world == null) return;
        synchronized (TEST_ENABLED) {
            TEST_ENABLED.put(world, enabled);
        }
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
            if (removed) {
                System.out.println("[TemporalTrials] Debug override cleared for world " + world.getRegistryKey());
            }
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
        return world.getRegistryKey().equals(TemporalTrialsMod.TEMPORAL_TRIALS_DIMENSION);
    }

    public static void forceStartLoop(ServerWorld world) {
        if (world == null) return;
        loopStartTick = world.getTimeOfDay();
        loopActive = true;
        System.out.println("[TemporalTrials] Loop forced start at tick " + loopStartTick + " for world " + world.getRegistryKey());
        sendStateToAll(world, true);
    }

    public static void forceStopLoop(ServerWorld world) {
        if (world == null) return;
        loopActive = false;
        System.out.println("[TemporalTrials] Loop forced stop for world " + world.getRegistryKey());
        sendStateToAll(world, false);
    }

    public static int getCurrentDayForWorld(ServerWorld world) {
        if (world == null) return 1;
        long now = world.getTimeOfDay();
        long elapsed = (loopStartTick < 0) ? 0 : now - loopStartTick;
        return getCurrentDay(elapsed);
    }

    public static void startLoop(ServerWorld world) {
        if (!isTemporalTrials(world)) return;
        loopStartTick = world.getTimeOfDay();
        loopActive = true;
        world.getPlayers().forEach(p -> p.sendMessage(Text.of("Dawn of the First Day..."), false));
        sendStateToAll(world, true);
    }

    public static void tick(ServerWorld world) {
        if (!isTemporalTrials(world)) return;
        if (!loopActive) {
            startLoop(world);
            return;
        }
        long now = world.getTimeOfDay();
        long elapsed = now - loopStartTick;
        if (elapsed < 0) {
            loopStartTick = now;
            elapsed = 0;
        }
        int currentDay = getCurrentDay(elapsed);
        long remaining = LOOP_DURATION_TICKS - elapsed;
        sendStateToAll(world, false, currentDay, remaining);
        handleDayTransitions(world, currentDay, elapsed);
        if (elapsed >= LOOP_DURATION_TICKS) {
            handleLoopEnd(world);
        }
    }

    private static int getCurrentDay(long elapsed) {
        if (elapsed < DAY_LENGTH_TICKS) return 1;
        if (elapsed < 2 * DAY_LENGTH_TICKS) return 2;
        return 3;
    }

    private static void handleDayTransitions(ServerWorld world, int currentDay, long elapsed) {
        if (elapsed == 0) return;
        if (elapsed == DAY_LENGTH_TICKS) {
            world.getPlayers().forEach(p -> p.sendMessage(Text.of("Dawn of the Second Day..."), false));
        } else if (elapsed == 2 * DAY_LENGTH_TICKS) {
            world.getPlayers().forEach(p -> p.sendMessage(Text.of("Dawn of the Final Day..."), false));
        }
    }

    private static void handleLoopEnd(ServerWorld world) {
	    // TODO: real win condition (Elytra, etc.) in a later portion
        boolean averted = checkWinCondition(world);
        if (!averted) {
            triggerMoonfall(world);
        }
        resetLoop(world);
    }

    private static boolean checkWinCondition(ServerWorld world) {
        return false; // Placeholder for Elytra logic
    }

    private static void triggerMoonfall(ServerWorld world) {
        world.getPlayers().forEach(p -> {
            p.sendMessage(Text.of("The moon crashes down..."), false);
            BlockPos pos = p.getBlockPos();
            world.createExplosion(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    6.0f,
                    World.ExplosionSourceType.TNT
            );
        });
    }

    private static void restoreInventory(ServerPlayerEntity player) {
        PlayerDataComponent data = PlayerDataProvider.get(player);
        DefaultedList<ItemStack> saved = data.getSavedInventory();
        for (int i = 0; i < saved.size(); i++) {
            player.getInventory().main.set(i, saved.get(i).copy());
        }
    }

    private static void resetLoop(ServerWorld world) {
        loopActive = false;
        long current = world.getTimeOfDay();
        long newTime = current - (current % DAY_LENGTH_TICKS);
        world.setTimeOfDay(newTime);
        BlockPos spawn = world.getSpawnPos();
        for (ServerPlayerEntity player : world.getPlayers()) {
            restoreInventory(player);
            player.requestTeleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
            player.setHealth(player.getMaxHealth());
        }
        startLoop(world);
    }

    public static void manualRewind(ServerWorld world, ServerPlayerEntity player) {
        long current = world.getTimeOfDay();
        long newTime = current - (current % DAY_LENGTH_TICKS);
        world.setTimeOfDay(newTime);
        BlockPos spawn = world.getSpawnPos();
        player.requestTeleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
    }

    // server -> client state packet sender (Option A: PacketByteBuf + ServerPlayNetworking)
    private static void sendStateToAll(ServerWorld world, boolean showIntro) {
        long now = world.getTimeOfDay();
        long elapsed = (loopStartTick < 0) ? 0 : now - loopStartTick;
        int day = getCurrentDay(elapsed);
        long remaining = Math.max(0, LOOP_DURATION_TICKS - elapsed);
        sendStateToAll(world, showIntro, day, remaining);
    }

    private static void sendStateToAll(ServerWorld world, boolean showIntro, int day, long remainingTicks) {
        LoopStatePayload payload = new LoopStatePayload(loopActive, day, remainingTicks, showIntro);
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}