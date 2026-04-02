package com.aric3435.temporaltrials.datagen;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class DataGeneratorMain {
    public static void main(String[] args) {
        try {
            Path out = Path.of("build/generated-data/temporal_trials/data/temporal_trials");
            Files.createDirectories(out);

            StructureProvider.generate(out.resolve("worldgen/structure"));
            TemplatePoolProvider.generate(out.resolve("worldgen/template_pool"));
            PlacedFeatureProvider.generate(out.resolve("worldgen/placed_feature"));
            WorldPresetProvider.generate(out.resolve("worldgen/world_preset"));
            DimensionProvider.generate(out.resolve("worldgen/dimension"));
            StructureNbtProvider.generate(out.resolve("structures"));

            System.out.println("Datagen finished: " + out.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Datagen failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
