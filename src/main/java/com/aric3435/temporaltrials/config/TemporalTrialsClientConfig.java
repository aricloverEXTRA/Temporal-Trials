package com.aric3435.temporaltrials.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * TemporalTrialsClientConfig: CLIENT-SIDE ONLY
 * 
 * These settings are stored locally on the client
 * They do NOT affect server behavior
 * Server settings are controlled by the server administrator
 * 
 * CLIENT-ONLY SETTINGS:
 * - Background music volume
 * - Chat message display
 * - Debug information
 */
@Environment(EnvType.CLIENT)
public final class TemporalTrialsClientConfig {
    private TemporalTrialsClientConfig() {}

    // ===== CLIENT-SIDE ONLY SETTINGS =====

    /**
     * Enable background music playback
     * Default: true
     */
    public static boolean BACKGROUND_MUSIC_ENABLED = true;

    /**
     * Music volume (0.0 - 1.0)
     * Default: 1.0 (full volume)
     */
    public static float MUSIC_VOLUME = 1.0f;

    /**
     * Show day transition messages in chat
     * Default: true
     */
    public static boolean SHOW_CHAT_MESSAGES = true;

    /**
     * Show debug cycle information on screen
     * Default: false
     */
    public static boolean SHOW_DEBUG_INFO = false;

    // ===== FILE MANAGEMENT =====

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

    /**
     * Load configuration from file
     * CLIENT-SIDE ONLY
     */
    public static void loadConfig() {
        if (CONFIG_PATH == null) {
            System.out.println("[TemporalTrials] ⚠ Cannot load config - invalid path");
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
                
                System.out.println("[TemporalTrials] ✓ Client config loaded from: " + CONFIG_PATH.toAbsolutePath());
            } else {
                System.out.println("[TemporalTrials] No config file found - using defaults");
                saveConfig();
            }
        } catch (Exception e) {
            System.err.println("[TemporalTrials] ✗ Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save configuration to file
     * CLIENT-SIDE ONLY
     */
    public static void saveConfig() {
        if (CONFIG_PATH == null) {
            System.err.println("[TemporalTrials] ✗ Cannot save config - invalid path");
            return;
        }

        try {
            ClientConfigData data = new ClientConfigData(
                    BACKGROUND_MUSIC_ENABLED,
                    MUSIC_VOLUME,
                    SHOW_CHAT_MESSAGES,
                    SHOW_DEBUG_INFO
            );
            
            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);
            
            System.out.println("[TemporalTrials] ✓ Client config saved to: " + CONFIG_PATH.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[TemporalTrials] ✗ Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * JSON data structure for client config
     */
    static class ClientConfigData {
        public boolean backgroundMusicEnabled;
        public float musicVolume;
        public boolean showChatMessages;
        public boolean showDebugInfo;

        public ClientConfigData(boolean backgroundMusicEnabled, float musicVolume, 
                               boolean showChatMessages, boolean showDebugInfo) {
            this.backgroundMusicEnabled = backgroundMusicEnabled;
            this.musicVolume = musicVolume;
            this.showChatMessages = showChatMessages;
            this.showDebugInfo = showDebugInfo;
        }

        // No-arg constructor for GSON
        public ClientConfigData() {}
    }
}