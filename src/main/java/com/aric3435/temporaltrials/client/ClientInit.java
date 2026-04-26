package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * ClientInit: registers client receiver and initializes client-only features.
 */
@Environment(EnvType.CLIENT)
public final class ClientInit implements ClientModInitializer {
    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        if (initialized) return;
        initialized = true;

        System.out.println("[TemporalTrials] Client initializing...");

        // Load client config
        TemporalTrialsClientConfig.loadConfig();
        System.out.println("[TemporalTrials] ✓ Client config loaded");

        // Register typed play-payload receiver (LoopStatePayload.ID)
        ClientPlayNetworking.registerGlobalReceiver(LoopStatePayload.ID, (payload, context) -> {
            // Execute on client thread
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                ClientLoopState.update(
                    payload.isActive(),
                    payload.getDay(),
                    payload.getRemainingTicks(),
                    payload.shouldShowIntro()
                );
            });
        });

        // Optional client systems (no-op stubs or real logic)
        MusicController.register();
        MoonHudRenderer.register();

        System.out.println("[TemporalTrials] ✓ Client initialized successfully");
    }
}