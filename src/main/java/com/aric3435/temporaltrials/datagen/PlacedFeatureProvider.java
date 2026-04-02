package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class PlacedFeatureProvider {
    public static void generate(Path out) throws IOException {
        Files.createDirectories(out);
        JsonObject root = new JsonObject();
        root.addProperty("feature", "minecraft:patch_grass");
        root.add("config", new JsonObject());
        JsonArray placement = new JsonArray();
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:count");
        count.addProperty("count", 3);
        placement.add(count);
        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        placement.add(inSquare);
        JsonObject heightmap = new JsonObject();
        heightmap.addProperty("type", "minecraft:heightmap");
        heightmap.addProperty("heightmap", "WORLD_SURFACE_WG");
        placement.add(heightmap);
        root.add("placement", placement);
        Files.writeString(out.resolve("pale_garden_patch.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
