package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.world.LoopManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class TemporalTrialsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(
                LoopStatePayload.ID,
                (payload, context) -> {
                    LoopStateClientState.update(payload);
                }
        );

//      HudRenderer.register();
        MusicController.register();
    }
}