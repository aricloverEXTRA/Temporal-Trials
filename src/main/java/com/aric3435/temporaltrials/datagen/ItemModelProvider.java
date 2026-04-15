package com.aric3435.temporaltrials.datagen;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * ItemModelProvider: Generates item model JSON files for proper item rendering
 * 
 * Creates models/item/*.json files that reference textures
 * Models must match the item IDs exactly
 */
public class ItemModelProvider {
    public static void generate(Path out) throws Exception {
        Path modelsDir = out.resolve("assets/temporal_trials/models/item");
        Files.createDirectories(modelsDir);
        
        System.out.println("[DataGen] Generating item models to: " + modelsDir.toAbsolutePath());
        
        // Flute of Time - handheld item (like a sword)
        createItemModel(
            modelsDir.resolve("flute_of_time.json"),
            "item/handheld",
            "temporal_trials:item/flute_of_time"
        );
        System.out.println("[DataGen] ✓ Generated flute_of_time.json");
        
        // Music Discs - flat circular items
        createItemModel(
            modelsDir.resolve("day_1_disc.json"),
            "item/generated",
            "temporal_trials:item/day_1_disc"
        );
        System.out.println("[DataGen] ✓ Generated day_1_disc.json");
        
        createItemModel(
            modelsDir.resolve("day_2_disc.json"),
            "item/generated",
            "temporal_trials:item/day_2_disc"
        );
        System.out.println("[DataGen] ✓ Generated day_2_disc.json");
        
        createItemModel(
            modelsDir.resolve("day_3_disc.json"),
            "item/generated",
            "temporal_trials:item/day_3_disc"
        );
        System.out.println("[DataGen] ✓ Generated day_3_disc.json");
    }

    /**
     * Create an item model JSON file
     * 
     * @param path Path to write the JSON file to
     * @param parent Parent model type (e.g., "item/handheld", "item/generated")
     * @param textureLayer0 Texture path (e.g., "temporal_trials:item/flute_of_time")
     */
    private static void createItemModel(Path path, String parent, String textureLayer0) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);
        
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", textureLayer0);
        root.add("textures", textures);
        
        // Format JSON nicely for readability
        String jsonContent = root.toString();
        
        Files.writeString(
            path,
            jsonContent,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}