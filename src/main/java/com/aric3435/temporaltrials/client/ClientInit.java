package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public final class ClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[TemporalTrials] client init - adding items to vanilla tab");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(TemporalTrialsMod.FLUTE_OF_TIME);
            entries.add(TemporalTrialsMod.DAY_1_DISC);
            entries.add(TemporalTrialsMod.DAY_2_DISC);
            entries.add(TemporalTrialsMod.DAY_3_DISC);
        });

        // Typed payload receiver (correct signature for your mappings)
        ClientPlayNetworking.registerGlobalReceiver(
                LoopStatePayload.ID,
                (client, payload, context) -> {
                    client.execute(() -> {
                        LoopStateClientState.active = payload.isActive();
                        LoopStateClientState.day = payload.getDay();
                        LoopStateClientState.remainingTicks = payload.getRemainingTicks();
                        LoopStateClientState.showIntro = payload.shouldShowIntro();

                        System.out.println(
                                "[TemporalTrials] client received loop_state: " +
                                "active=" + payload.isActive() +
                                " day=" + payload.getDay() +
                                " remaining=" + payload.getRemainingTicks() +
                                " intro=" + payload.shouldShowIntro()
                        );
                    });
                }
        );

        System.out.println("[TemporalTrials] client receiver registered for LoopStatePayload");
    }
}