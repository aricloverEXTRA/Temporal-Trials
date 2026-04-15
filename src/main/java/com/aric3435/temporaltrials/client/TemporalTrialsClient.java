package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.network.LoopStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class TemporalTrialsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[TemporalTrials] Client initializing...");

        ClientPlayNetworking.registerGlobalReceiver(
                LoopStatePayload.ID,
                (payload, context) -> {
                    // Direct assignment - no update() method call
                    LoopStateClientState.active = payload.isActive();
                    LoopStateClientState.day = payload.getDay();
                    LoopStateClientState.remainingTicks = payload.getRemainingTicks();
                    LoopStateClientState.showIntro = payload.shouldShowIntro();

                    System.out.println(
                            "[TemporalTrials] Client received loop state: " +
                            "active=" + payload.isActive() +
                            " day=" + payload.getDay() +
                            " remaining=" + payload.getRemainingTicks()
                    );
                }
        );

        MusicController.register();
        System.out.println("[TemporalTrials] Client initialized");
    }
}