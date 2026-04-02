package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StructureProvider {
    public static void generate(Path out) throws IOException {
        Path dir = out;
        Files.createDirectories(dir);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:jigsaw");
        root.addProperty("start_pool", "temporal_trials:elytra_relic/start");
        root.addProperty("size", 1);
        root.addProperty("step", "surface_structures");
        root.addProperty("terrain_adaptation", "beard_thin");
        JsonObject startHeight = new JsonObject();
        startHeight.addProperty("absolute", 0);
        root.add("start_height", startHeight);
        root.addProperty("project_start_to_heightmap", "WORLD_SURFACE_WG");
        root.addProperty("max_distance_from_center", 80);
        Files.writeString(dir.resolve("elytra_relic.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
