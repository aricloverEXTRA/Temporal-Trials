package com.aric3435.temporaltrials.datagen;

import java.nio.file.Path;

/**
 * Datagen for Temporal Trials
 * 
 * Generates:
 * - Item models (for proper rendering)
 * 
 * Does NOT generate (already in resources):
 * - Recipes (flute_of_time.json - hand-crafted)
 * - Worldgen (removed - too complex for now)
 * - Structures (removed - causes errors)
 */
public class DataGeneratorMain {
    public static void main(String[] args) {
        try {
            Path assetsOut = Path.of("build/generated-data/temporal_trials");
            
            // Only generate item models
            ItemModelProvider.generate(assetsOut);
            System.out.println("[DataGen] ✓ Datagen finished - Item models generated");
        } catch (Exception e) {
            System.err.println("[DataGen] ✗ Datagen failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}