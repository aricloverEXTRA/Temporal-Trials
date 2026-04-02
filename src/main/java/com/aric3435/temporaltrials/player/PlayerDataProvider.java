package com.aric3435.temporaltrials.player;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDataProvider {

    public static PlayerDataComponent get(ServerPlayerEntity player) {
        // Temporary in-memory storage for 0.1.0-beta
        return new PlayerDataComponent();
    }

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // No persistence yet
        });
    }
}
