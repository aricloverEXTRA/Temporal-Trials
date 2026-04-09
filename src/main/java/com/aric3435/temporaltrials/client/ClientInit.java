package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * ClientInit: Client-side initialization for Temporal Trials
 * 
 * CLIENT-SIDE ONLY
 * Registers:
 * - Network payload receivers
 * - Music controller
 * - Client-side config loading
 */
@Environment(EnvType.CLIENT)
public final class ClientInit implements ClientModInitializer {

    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        // Prevent double initialization
        if (initialized) {
            return;
        }
        initialized = true;

        System.out.println("[TemporalTrials] Client initializing...");

        // Load client-side configuration
        TemporalTrialsClientConfig.loadConfig();
        System.out.println("[TemporalTrials] ✓ Client config loaded");

        // Register network payload receiver
        ClientPlayNetworking.registerGlobalReceiver(
                LoopStatePayload.ID,
                (payload, context) -> {
                    // Update client state directly
                    LoopStateClientState.active = payload.isActive();
                    LoopStateClientState.day = payload.getDay();
                    LoopStateClientState.remainingTicks = payload.getRemainingTicks();
                    LoopStateClientState.showIntro = payload.shouldShowIntro();
                }
        );

        // Register music controller
        MusicController.register();

        System.out.println("[TemporalTrials] ✓ Client initialized successfully");
    }
}