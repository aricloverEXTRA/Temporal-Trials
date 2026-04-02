package com.aric3435.temporaltrials;

import com.aric3435.temporaltrials.command.TemporalTrialsCommand; // debug command
import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.item.FluteOfTimeItem;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import com.aric3435.temporaltrials.sound.ModSounds;
import com.aric3435.temporaltrials.world.LoopManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.item.Item;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;

public class TemporalTrialsMod implements ModInitializer {

    public static final String MOD_ID = "temporal_trials";

    // World preset + dimension keys
    public static final RegistryKey<WorldPreset> TEMPORAL_TRIALS_PRESET =
            RegistryKey.of(RegistryKeys.WORLD_PRESET, id("temporal_trials"));

    public static final RegistryKey<DimensionOptions> TEMPORAL_TRIALS_DIMENSION =
            RegistryKey.of(RegistryKeys.DIMENSION, id("temporal_trials_overworld"));

    // Gamerule
    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_END =
            GameRuleRegistry.register(
                    "disableEnd",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    // Items
    public static Item FLUTE_OF_TIME;
    public static Item DAY_1_DISC;
    public static Item DAY_2_DISC;
    public static Item DAY_3_DISC;

    @Override
    public void onInitialize() {
        // Persistence stub
	    PayloadTypeRegistry.playS2C().register(LoopStatePayload.ID, LoopStatePayload.CODEC);
        PlayerDataProvider.register();

        // Centralized sound registration (ModSounds handles SoundEvent and JukeboxSong registration)
        ModSounds.register();

        // Register Flute of Time
        Identifier fluteId = id("flute_of_time");
        RegistryKey<Item> fluteKey = RegistryKey.of(RegistryKeys.ITEM, fluteId);

        FLUTE_OF_TIME = Registry.register(
                Registries.ITEM,
                fluteId,
                new FluteOfTimeItem(
                        new Item.Settings()
                                .registryKey(fluteKey)
                                .maxCount(1)
                )
        );

        // Register music discs as plain Items with jukeboxPlayable(soundEvent)
        DAY_1_DISC = registerDisc("day_1_disc", ModSounds.DAY_1_KEY, 4);
        DAY_2_DISC = registerDisc("day_2_disc", ModSounds.DAY_2_KEY, 4);
        DAY_3_DISC = registerDisc("day_3_disc", ModSounds.DAY_3_KEY, 4);

        // Loop tick handler
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (LoopManager.isTemporalTrials(world)) {
                LoopManager.tick(world);
            }
        });

        // Register debug command (debug only) so you can toggle the mode in dev
        TemporalTrialsCommand.register();

        System.out.println("[Temporal Trials] Initialized successfully.");
    }

    // Helper: Identifier
    private static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    // Helper: Disc registration using Item.Settings().jukeboxPlayable(soundEvent)
    private static Item registerDisc(String discName, RegistryKey<JukeboxSong> jukeboxKey, int comparatorOutput) {
        Identifier id = id(discName); // e.g., temporal_trials:day_1_disc
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

        Item disc = new Item(new Item.Settings()
                .registryKey(key)
                .maxCount(1)
				.jukeboxPlayable(jukeboxKey)
        );

        return Registry.register(Registries.ITEM, id, disc);
    }
}
