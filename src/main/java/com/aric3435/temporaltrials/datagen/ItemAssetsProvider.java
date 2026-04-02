package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ItemAssetsProvider {
    public static void generate(Path out) throws Exception {
        Path models = out.resolve("assets/temporal_trials/models/item");
        Files.createDirectories(models);
        writeModel(models.resolve("flute_of_time.json"), "temporal_trials:item/flute_of_time");
        writeModel(models.resolve("day_1_disc.json"), "temporal_trials:item/day_1_disc");
        writeModel(models.resolve("day_2_disc.json"), "temporal_trials:item/day_2_disc");
        writeModel(models.resolve("day_3_disc.json"), "temporal_trials:item/day_3_disc");
    }

    private static void writeModel(Path p, String texture) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "item/generated");
        JsonObject tex = new JsonObject();
        tex.addProperty("layer0", texture);
        root.add("textures", tex);
        Files.writeString(p, root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}