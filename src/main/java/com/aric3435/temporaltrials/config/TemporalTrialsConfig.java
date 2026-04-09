package com.aric3435.temporaltrials.config;

/**
 * TemporalTrialsConfig: Configuration for Temporal Trials
 * 
 * Features:
 * - Adjustable cycle length (3, 5, 7 days, etc.)
 * - Music adapts to cycle length
 * - Day 3 (final day) always plays final music
 * - Multiplayer lives system
 * - Background music toggle
 * - Prevent single player from failing cycle (multiplayer only)
 */
public final class TemporalTrialsConfig {
    private TemporalTrialsConfig() {}

    /**
     * Enable multiplayer lives system (prevents griefing, enables progression)
     * 2+ players = extra lives per player
     * Default: true (enabled)
     */
    public static boolean MULTIPLAYER_LIVES_ENABLED = true;

    /**
     * MULTIPLAYER ONLY: Prevent single player death from resetting cycle
     * 
     * When enabled:
     * - Player dies → moved to spectator mode
     * - Cycle continues (other players can try to win)
     * - Only when ALL players are dead does cycle reset
     * 
     * When disabled (default):
     * - Any player death (with no lives) → cycle resets
     * - Classic hardcore mode
     * 
     * This option only works in multiplayer (2+ players)
     * Single player servers ignore this setting
     * Default: true (enabled for multiplayer friendliness)
     */
    public static boolean PREVENT_SINGLE_DEATH_RESET = true;

    /**
     * Enable background music for Temporal Trials gamemode
     * Default: true (enabled)
     */
    public static boolean BACKGROUND_MUSIC_ENABLED = true;

    /**
     * CYCLE LENGTH: Total number of in-game days per cycle
     * Default: 3 (Majora's Mask style)
     * 
     * Examples:
     * - 3 = Day 1, Day 2, Day 3 (classic - 72,000 ticks)
     * - 5 = Day 1, Day 2, Day 3, Day 4, Day 5 (harder - 120,000 ticks)
     * - 7 = Day 1-7 (extreme - 168,000 ticks)
     * 
     * Music distribution:
     * - Days 1 to (CYCLE_LENGTH_DAYS/2): Play DAY_1 music
     * - Days (CYCLE_LENGTH_DAYS/2) to CYCLE_LENGTH_DAYS-1: Play DAY_2 music
     * - Day CYCLE_LENGTH_DAYS: Always plays DAY_3 music (FINAL)
     */
    public static int CYCLE_LENGTH_DAYS = 3;

    /**
     * Chunk regeneration radius (in chunks)
     * Default: 32 chunks (~512 blocks)
     * Higher = more intensive, takes longer
     */
    public static int CHUNK_REGEN_RADIUS = 32;

    /**
     * Flute of Time cooldown in ticks
     * Default: 36000 (1.5 in-game days)
     */
    public static int FLUTE_COOLDOWN_TICKS = 36000;

    /**
     * Enable debug commands (/temporaltrials set/toggle)
     * Default: true (enabled for development)
     */
    public static boolean DEBUG_COMMANDS_ENABLED = true;

    // ===== CALCULATED VALUES =====

    /**
     * Total cycle duration in ticks (adjusted by CYCLE_LENGTH_DAYS)
     * Formula: 24000 ticks/day * CYCLE_LENGTH_DAYS
     * 
     * Examples:
     * - 3 days = 72,000 ticks
     * - 5 days = 120,000 ticks
     * - 7 days = 168,000 ticks
     */
    public static long getLoopDurationTicks() {
        return 24000L * CYCLE_LENGTH_DAYS;
    }

    /**
     * Single day duration in ticks (always constant)
     * 24000 ticks = 1 in-game day
     */
    public static long getDayLengthTicks() {
        return 24000L;
    }

    /**
     * Get which music track should play for a given day number
     * 
     * Music distribution strategy:
     * - Early days → DAY_1 music (exploration phase)
     * - Middle days → DAY_2 music (escalation phase)
     * - Final day → DAY_3 music (always, crisis phase)
     * 
     * Examples (with CYCLE_LENGTH_DAYS = 3):
     * - Day 1 → DAY_1 music
     * - Day 2 → DAY_2 music
     * - Day 3 → DAY_3 music (FINAL)
     * 
     * Examples (with CYCLE_LENGTH_DAYS = 5):
     * - Day 1 → DAY_1 music
     * - Day 2 → DAY_1 music (repeats/stretches)
     * - Day 3 → DAY_2 music
     * - Day 4 → DAY_2 music (repeats/stretches)
     * - Day 5 → DAY_3 music (FINAL)
     */
    public static int getMusicTrackForDay(int currentDay) {
        if (currentDay >= CYCLE_LENGTH_DAYS) {
            return 3;
        }

        int daysPerTrack = Math.max(1, (CYCLE_LENGTH_DAYS - 1) / 2);

        if (currentDay <= daysPerTrack) {
            return 1;
        } else if (currentDay < CYCLE_LENGTH_DAYS) {
            return 2;
        }

        return 3;
    }

    /**
     * Get day as a friendly string
     */
    public static String getDayName(int day) {
        int maxDay = CYCLE_LENGTH_DAYS;
        
        if (day >= maxDay) {
            return "Final Day";
        }

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

    /**
     * Load configuration from file
     * TODO: Integrate with Mod Menu and config files
     */
    public static void loadConfig() {
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("[TemporalTrials] Config loaded (SERVER-SIDE ONLY):");
        System.out.println("[TemporalTrials]   - Cycle Length: " + CYCLE_LENGTH_DAYS + " days");
        System.out.println("[TemporalTrials]   - Total Duration: " + (getLoopDurationTicks() / 24000) + " days (" + getLoopDurationTicks() + " ticks)");
        System.out.println("[TemporalTrials]   - Multiplayer Lives: " + (MULTIPLAYER_LIVES_ENABLED ? "ENABLED" : "DISABLED"));
        System.out.println("[TemporalTrials]   - Prevent Single Death Reset: " + (PREVENT_SINGLE_DEATH_RESET ? "ENABLED" : "DISABLED"));
        System.out.println("[TemporalTrials]   - Chunk Regen Radius: " + CHUNK_REGEN_RADIUS + " chunks");
        System.out.println("[TemporalTrials]   - Flute Cooldown: " + FLUTE_COOLDOWN_TICKS + " ticks (" + (FLUTE_COOLDOWN_TICKS / 24000.0) + " days)");
        System.out.println("[TemporalTrials]   - Debug Commands: " + (DEBUG_COMMANDS_ENABLED ? "ENABLED" : "DISABLED"));
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("[TemporalTrials] ℹ Server settings cannot be overridden by clients");
    }
}