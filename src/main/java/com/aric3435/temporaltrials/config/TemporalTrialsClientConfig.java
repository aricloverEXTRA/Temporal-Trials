package com.aric3435.temporaltrials.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TemporalTrialsClientConfig: CLIENT-SIDE ONLY
 */
@Environment(EnvType.CLIENT)
public final class TemporalTrialsClientConfig {
    private TemporalTrialsClientConfig() {}

    public static boolean BACKGROUND_MUSIC_ENABLED = true;
    public static float MUSIC_VOLUME = 1.0f;
    public static boolean SHOW_CHAT_MESSAGES = true;
    public static boolean SHOW_DEBUG_INFO = false;

    // New options
    public static boolean SHOW_DAY_TRANSITION_SCREEN = true;
    public static boolean HIDE_CLOCK_DURING_TRANSITION = true;

    private static final Path CONFIG_PATH = getConfigPath();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getConfigPath() {
        try {
            Path configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
            return configDir.resolve("temporal_trials_client.json");
        } catch (Exception e) {
            System.err.println("[TemporalTrials] Failed to get config path: " + e.getMessage());
            return null;
        }
    }

    public static void loadConfig() {
        if (CONFIG_PATH == null) {
            System.out.println("[TemporalTrials] ⚠ Cannot load client config - invalid path");
            return;
        }

        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                ClientConfigData data = GSON.fromJson(json, ClientConfigData.class);

                BACKGROUND_MUSIC_ENABLED = data.backgroundMusicEnabled;
                MUSIC_VOLUME = Math.max(0.0f, Math.min(1.0f, data.musicVolume));
                SHOW_CHAT_MESSAGES = data.showChatMessages;
                SHOW_DEBUG_INFO = data.showDebugInfo;

                SHOW_DAY_TRANSITION_SCREEN = data.showDayTransitionScreen;
                HIDE_CLOCK_DURING_TRANSITION = data.hideClockDuringTransition;

                System.out.println("[TemporalTrials] ✓ Client config loaded from: " + CONFIG_PATH.toAbsolutePath());
            } else {
                System.out.println("[TemporalTrials] No client config found - using defaults");
                saveConfig();
            }
        } catch (Exception e) {
            System.err.println("[TemporalTrials] ✗ Failed to load client config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        if (CONFIG_PATH == null) {
            System.err.println("[TemporalTrials] ✗ Cannot save client config - invalid path");
            return;
        }

        try {
            ClientConfigData data = new ClientConfigData(
                    BACKGROUND_MUSIC_ENABLED,
                    MUSIC_VOLUME,
                    SHOW_CHAT_MESSAGES,
                    SHOW_DEBUG_INFO,
                    SHOW_DAY_TRANSITION_SCREEN,
                    HIDE_CLOCK_DURING_TRANSITION
            );

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);

            System.out.println("[TemporalTrials] ✓ Client config saved to: " + CONFIG_PATH.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[TemporalTrials] ✗ Failed to save client config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ClientConfigData {
        public boolean backgroundMusicEnabled = true;
        public float musicVolume = 1.0f;
        public boolean showChatMessages = true;
        public boolean showDebugInfo = false;
        public boolean showDayTransitionScreen = true;
        public boolean hideClockDuringTransition = true;

        public ClientConfigData() {}

        public ClientConfigData(boolean backgroundMusicEnabled, float musicVolume, boolean showChatMessages, boolean showDebugInfo, boolean showDayTransitionScreen, boolean hideClockDuringTransition) {
            this.backgroundMusicEnabled = backgroundMusicEnabled;
            this.musicVolume = musicVolume;
            this.showChatMessages = showChatMessages;
            this.showDebugInfo = showDebugInfo;
            this.showDayTransitionScreen = showDayTransitionScreen;
            this.hideClockDuringTransition = hideClockDuringTransition;
        }
    }
}