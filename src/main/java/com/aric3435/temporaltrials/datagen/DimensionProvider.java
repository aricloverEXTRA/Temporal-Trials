package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class DimensionProvider {
    public static void generate(Path out) throws IOException {
        Files.createDirectories(out);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:overworld");
        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("settings", "minecraft:overworld");
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:fixed");
        biomeSource.addProperty("biome", "minecraft:plains");
        generator.add("biome_source", biomeSource);
        root.add("generator", generator);
        Files.writeString(out.resolve("temporal_trials_overworld.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}