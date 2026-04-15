package com.aric3435.temporaltrials.datagen;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class StructureNbtProvider {
    /**
     * If you already have src/main/resources/data/temporal_trials/structures/elytra_relic/ship.nbt,
     * this method copies it into the generated output. If you want programmatic NBT edits,
     * replace this copy logic with NBT read/modify/write using net.minecraft.nbt classes.
     */
    public static void generate(Path out) throws IOException {
        Path src = Path.of("src/main/resources/data/temporal_trials/structures/elytra_relic/ship.nbt");
        if (Files.exists(src)) {
            Files.createDirectories(out);
            Files.copy(src, out.resolve("elytra_relic/ship.nbt"), StandardCopyOption.REPLACE_EXISTING);
        } else {
            // no-op if structure NBT not present; create directories so datagen doesn't fail
            Files.createDirectories(out.resolve("elytra_relic"));
        }
    }
}
