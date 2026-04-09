package com.aric3435.temporaltrials.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Minimal Fabric datagen entrypoint
 * Just generates item models, nothing else
 */
public class FabricDatagenEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        // We handle everything in DataGeneratorMain
        System.out.println("[DataGen] Fabric datagen entrypoint loaded");
    }
}