package com.aric3435.temporaltrials;

import com.aric3435.temporaltrials.command.TemporalTrialsCommand;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.network.LoopStatePayload;
import com.aric3435.temporaltrials.item.FluteOfTimeItem;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import com.aric3435.temporaltrials.player.PlayerDeathHandler;
import com.aric3435.temporaltrials.sound.ModSounds;
import com.aric3435.temporaltrials.world.LoopManager;
import com.aric3435.temporaltrials.world.WorldStateManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.GameRules;

/**
 * TemporalTrialsMod: Main mod initializer
 * 
 * Majora's Mask inspired gamemode with 3-day cycles
 * Fabric 1.21.4 Edition
 * 
 * Accessed via: /temporaltrials set true/false
 */
public class TemporalTrialsMod implements ModInitializer {

    public static final String MOD_ID = "temporal_trials";

    /**
     * Dimension identifier for Temporal Trials
     * Used to check if a world is in Temporal Trials mode
     */
    public static final RegistryKey<?> TEMPORAL_TRIALS_DIMENSION =
            RegistryKey.of(RegistryKeys.WORLD, id("temporal_trials_overworld"));

    public static final GameRules.Key<GameRules.BooleanRule> DISABLE_END =
            GameRuleRegistry.register(
                    "disableEnd",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    // ===== ITEM REGISTRATIONS =====
    public static Item FLUTE_OF_TIME;
    public static Item DAY_1_DISC;
    public static Item DAY_2_DISC;
    public static Item DAY_3_DISC;

    // ===== CUSTOM ITEM GROUP =====
    public static ItemGroup TEMPORAL_TRIALS_GROUP;

    @Override
    public void onInitialize() {
        System.out.println("");
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("[TemporalTrials] ╔════ Initializing Temporal Trials ════╗");
        System.out.println("[TemporalTrials] ║   Inspired by Majora's Mask         ║");
        System.out.println("[TemporalTrials] ║   Fabric 1.21.4 Edition             ║");
        System.out.println("[TemporalTrials] ╚════════════════════════════════════════╝");
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("");

        // Load configuration
        TemporalTrialsConfig.loadConfig();

        // Register network payload
        PayloadTypeRegistry.playS2C().register(LoopStatePayload.ID, LoopStatePayload.CODEC);
        System.out.println("[TemporalTrials] ✓ Network payload registered");

        // Register player handlers
        PlayerDataProvider.register();
        PlayerDeathHandler.register();
        System.out.println("[TemporalTrials] ✓ Player data & death handler registered");

        // Register sounds
        ModSounds.register();
        System.out.println("[TemporalTrials] ✓ Sounds registered");

        // ===== REGISTER ITEMS =====
        System.out.println("[TemporalTrials] Registering items...");

        // Register Flute of Time
        Identifier fluteId = id("flute_of_time");
        RegistryKey<Item> fluteKey = RegistryKey.of(RegistryKeys.ITEM, fluteId);
        FLUTE_OF_TIME = Registry.register(
                Registries.ITEM,
                fluteId,
                new FluteOfTimeItem(
                        new Item.Settings()
								.rarity(Rarity.RARE)
                                .registryKey(fluteKey)
                                .maxCount(1)
                )
        );
        System.out.println("[TemporalTrials] ✓ Registered: flute_of_time");

        // Register Day 1 Music Disc
        Identifier day1Id = id("day_1_disc");
        RegistryKey<Item> day1Key = RegistryKey.of(RegistryKeys.ITEM, day1Id);
        DAY_1_DISC = Registry.register(
                Registries.ITEM,
                day1Id,
                new Item(new Item.Settings()
						.rarity(Rarity.RARE)
                        .registryKey(day1Key)
                        .maxCount(1)
                        .jukeboxPlayable(ModSounds.DAY_1_KEY)
                )
        );
        System.out.println("[TemporalTrials] ✓ Registered: day_1_disc");

        // Register Day 2 Music Disc
        Identifier day2Id = id("day_2_disc");
        RegistryKey<Item> day2Key = RegistryKey.of(RegistryKeys.ITEM, day2Id);
        DAY_2_DISC = Registry.register(
                Registries.ITEM,
                day2Id,
                new Item(new Item.Settings()
						.rarity(Rarity.RARE)
                        .registryKey(day2Key)
                        .maxCount(1)
                        .jukeboxPlayable(ModSounds.DAY_2_KEY)
                )
        );
        System.out.println("[TemporalTrials] ✓ Registered: day_2_disc");

        // Register Day 3 Music Disc
        Identifier day3Id = id("day_3_disc");
        RegistryKey<Item> day3Key = RegistryKey.of(RegistryKeys.ITEM, day3Id);
        DAY_3_DISC = Registry.register(
                Registries.ITEM,
                day3Id,
                new Item(new Item.Settings()
						.rarity(Rarity.RARE)
                        .registryKey(day3Key)
                        .maxCount(1)
                        .jukeboxPlayable(ModSounds.DAY_3_KEY)
                )
        );
        System.out.println("[TemporalTrials] ✓ Registered: day_3_disc");

        System.out.println("[TemporalTrials] ✓ All items registered");

        // ===== ADD ITEMS TO CREATIVE TABS =====
        System.out.println("[TemporalTrials] Adding items to creative tabs...");

        // Add Flute to TOOLS tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(FLUTE_OF_TIME);
            System.out.println("[TemporalTrials] ✓ Added Flute of Time to TOOLS tab");
        });

        // Add music discs to REDSTONE tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(DAY_1_DISC);
            entries.add(DAY_2_DISC);
            entries.add(DAY_3_DISC);
            System.out.println("[TemporalTrials] ✓ Added music discs to REDSTONE tab");
        });

        System.out.println("[TemporalTrials] ✓ Creative tabs updated");

        // Register world tick handler
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (LoopManager.isTemporalTrials(world)) {
                WorldStateManager.captureInitialWorldState(world);
                LoopManager.tick(world);
            }
        });
        System.out.println("[TemporalTrials] ✓ Server tick handler registered");

        // Register debug commands
        TemporalTrialsCommand.register();
        System.out.println("[TemporalTrials] ✓ Debug commands registered");

        System.out.println("");
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("[TemporalTrials] ✓ Initialization complete!");
        System.out.println("[TemporalTrials] ═══════════════════════════════════════════");
        System.out.println("");
    }

    private static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}