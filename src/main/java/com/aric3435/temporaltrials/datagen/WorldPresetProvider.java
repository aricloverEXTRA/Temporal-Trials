package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class WorldPresetProvider {
    public static void generate(Path out) throws IOException {
        Files.createDirectories(out);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:world_preset");
        JsonObject dims = new JsonObject();
        dims.addProperty("minecraft:overworld", "temporal_trials:temporal_trials_overworld");
        root.add("dimensions", dims);
        Files.writeString(out.resolve("temporal_trials.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
