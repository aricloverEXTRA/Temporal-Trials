package com.aric3435.temporaltrials.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Minimal Fabric datagen entrypoint.
 *
 * This delegates to your existing DataGeneratorMain so Fabric's datagen discovery
 * finds an entrypoint and the Fabric warning disappears. It does not attempt to
 * reimplement providers as Fabric providers; it simply runs your generator.
 */
public class FabricDatagenEntrypoint implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        try {
            // Delegate to your existing datagen main. It writes to build/generated-data/...
            com.aric3435.temporaltrials.datagen.DataGeneratorMain.main(new String[0]);
        } catch (Exception e) {
            // Re-throw as runtime so Fabric datagen fails loudly if generation fails
            throw new RuntimeException("Temporal Trials datagen failed", e);
        }
    }
}
