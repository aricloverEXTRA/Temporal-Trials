package com.aric3435.temporaltrials.config;

import net.minecraft.server.MinecraftServer;

public final class TemporalTrialsConfig {
    private TemporalTrialsConfig() {}

    // === Core gameplay options ===

    /** Enable/disable multiplayer lives (extra lives if 2+ players) */
    public static boolean MULTIPLAYER_LIVES_ENABLED = true;

    /** In multiplayer: When enabled, death puts player in spectator; cycle ends only if ALL are dead */
    public static boolean PREVENT_SINGLE_DEATH_RESET = true;

    /** Enable/disable background music (server side toggle) */
    public static boolean BACKGROUND_MUSIC_ENABLED = true;

    /** Number of in-game days per cycle (min 3, max 7 recommended) */
    public static int CYCLE_LENGTH_DAYS = 3;

    /** How far (in chunks) around spawn to reset/regenerate per cycle */
    public static int CHUNK_REGEN_RADIUS = 32;

    /** Flute of Time cooldown, in ticks (20 ticks = 1 second; 36000 = 1.5 Minecraft days) */
    public static int FLUTE_COOLDOWN_TICKS = 36000;

    /** Enable debug commands like /temporaltrials set/toggle (recommended true for dev/test) */
    public static boolean DEBUG_COMMANDS_ENABLED = true;

    // === Utility methods ===

    /** Returns true if multiplayer lives are meaningful given server and config (2+ players) */
    public static boolean isMultiplayerLivesEffective(MinecraftServer server) {
        return MULTIPLAYER_LIVES_ENABLED && (server != null) && server.getCurrentPlayerCount() > 1;
    }

    /** Human-readable day name */
    public static String getDayName(int day) {
        if (day >= CYCLE_LENGTH_DAYS) return "Final Day";
        return switch (day) {
            case 1 -> "First Day";
            case 2 -> "Second Day";
            case 3 -> "Third Day";
            case 4 -> "Fourth Day";
            case 5 -> "Fifth Day";
            case 6 -> "Sixth Day";
            case 7 -> "Seventh Day";
            default -> "Day " + day;
        };
    }

    /** Returns which "track" index to play for a day: 1, 2, or 3 */
    public static int getMusicTrackForDay(int currentDay) {
        if (currentDay >= CYCLE_LENGTH_DAYS) return 3;
        int daysPerTrack = Math.max(1, (CYCLE_LENGTH_DAYS - 1) / 2);
        if (currentDay <= daysPerTrack) return 1;
        else if (currentDay < CYCLE_LENGTH_DAYS) return 2;
        return 3;
    }

    /** Get ticks for 1 full cycle */
    public static long getLoopDurationTicks() {
        return 24000L * CYCLE_LENGTH_DAYS;
    }

    /** 1 day in ticks */
    public static long getDayLengthTicks() {
        return 24000L;
    }

    /** Show current config and context (for logging or /temporaltrials help) */
    public static String getHelpMessage(MinecraftServer server) {
        StringBuilder msg = new StringBuilder();
        msg.append("§6Temporal Trials Help\n");
        msg.append("§7/temporaltrials set <true|false> - Enable/disable Temporal Trials\n");
        msg.append("§7/temporaltrials toggle - Toggle Temporal Trials\n");
        if (MULTIPLAYER_LIVES_ENABLED) {
            msg.append("§7/temporaltrials lives <player> <amt>\n");
            msg.append("§7/temporaltrials lives add <player> <amt>\n");
            msg.append("§7/temporaltrials lives <amt> (all)\n");
        }
        msg.append("§7Cycle: Start Trials, craft Flute of Time, survive and explore.\n");
        if (server != null) {
            if (isMultiplayerLivesEffective(server)) {
                msg.append("§bMultiplayer: Each player has extra lives. When all are lost, the cycle resets.\n");
            } else {
                msg.append("§bSingleplayer: No extra lives. Death resets the cycle!\n");
            }
        }
        return msg.toString();
    }

    /** Print config summary to log (called at startup) */
    public static void loadConfig() {
        System.out.println("[TemporalTrials] ========= Config =========");
        System.out.println("[TemporalTrials]   Cycle length: " + CYCLE_LENGTH_DAYS + " days");
        System.out.println("[TemporalTrials]   Lives: " + (MULTIPLAYER_LIVES_ENABLED ? "ENABLED" : "DISABLED"));
        System.out.println("[TemporalTrials]   Prevent single reset: " + (PREVENT_SINGLE_DEATH_RESET ? "ENABLED" : "DISABLED"));
        System.out.println("[TemporalTrials]   Background music: " + (BACKGROUND_MUSIC_ENABLED ? "ON" : "OFF"));
        System.out.println("[TemporalTrials]   Chunk regen radius: " + CHUNK_REGEN_RADIUS + " chunks");
        System.out.println("[TemporalTrials]   Flute cooldown: " + FLUTE_COOLDOWN_TICKS + " ticks");
        System.out.println("[TemporalTrials]   Debug commands: " + (DEBUG_COMMANDS_ENABLED ? "ON" : "OFF"));
        System.out.println("[TemporalTrials] ==================================");
        System.out.println("[TemporalTrials] ℹ Server settings cannot be overridden by clients");
    }
}