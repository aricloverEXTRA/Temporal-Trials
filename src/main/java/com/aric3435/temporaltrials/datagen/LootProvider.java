package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LootProvider {
    public static void generate(Path out) throws Exception {
        Path loot = out.resolve("data/temporal_trials/loot_tables/chests");
        Files.createDirectories(loot);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:chest");
        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "temporal_trials:day_1_disc");
        entries.add(entry);
        pool.add("entries", entries);
        pools.add(pool);
        root.add("pools", pools);
        Files.writeString(loot.resolve("temporal_trials_disc_chest.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}