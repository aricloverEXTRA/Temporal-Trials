package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class TemplatePoolProvider {
    public static void generate(Path out) throws IOException {
        Files.createDirectories(out);
        JsonObject root = new JsonObject();
        root.addProperty("name", "temporal_trials:elytra_relic/start");
        root.addProperty("fallback", "minecraft:empty");
        JsonArray elements = new JsonArray();
        JsonObject el = new JsonObject();
        el.addProperty("weight", 1);
        JsonObject element = new JsonObject();
        element.addProperty("element_type", "minecraft:single_pool_element");
        element.addProperty("location", "temporal_trials:elytra_relic/ship");
        element.addProperty("processors", "minecraft:empty");
        element.addProperty("projection", "rigid");
        el.add("element", element);
        elements.add(el);
        root.add("elements", elements);
        Files.writeString(out.resolve("start.json"), root.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
