package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class ClientInit implements ClientModInitializer {
    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        if (initialized) return;
        initialized = true;

        System.out.println("[TemporalTrials] Client initializing...");

        TemporalTrialsClientConfig.loadConfig();

        // register typed payload receiver
        ClientPlayNetworking.registerGlobalReceiver(LoopStatePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientLoopState.update(payload.isActive(), payload.getDay(), payload.getRemainingTicks(), payload.shouldShowIntro());
                MoonFallController.onPayload(payload.shouldShowIntro(), payload.getDay(), payload.getRemainingTicks());
            });
        });

        // register controllers and HUDs
        ClientMusicController.register();
        MoonFallController.register();
        ClockHudRenderer.register();

        System.out.println("[TemporalTrials] ✓ Client initialized successfully");
    }
}