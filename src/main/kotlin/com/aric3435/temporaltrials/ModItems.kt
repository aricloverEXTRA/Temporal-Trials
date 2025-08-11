package com.aric3435.temporaltrials

import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.Item.Settings
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier
import net.minecraft.util.Rarity
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.block.jukebox.JukeboxSong

object ModItems {

    public static final SoundEvent DAY_1 = registerSoundEvent("day_1");
    public static final SoundEvent DAY_2 = registerSoundEvent("day_2");
    public static final SoundEvent DAY_3 = registerSoundEvent("day_3");

    private fun register(
        path: String,
        factory: (Settings) -> Item,
        settings: Settings
    ): Item {
        val key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Constants.MOD_ID, path))
        return Items.register(key, factory, settings)
    }

    private fun id(path: String) = Identifier.of(Constants.MOD_ID, path)

    private fun songKey(path: String): RegistryKey<JukeboxSong> =
        RegistryKey.of(RegistryKeys.JUKEBOX_SONG, "day_1"))

    private fun songKey(path: String): RegistryKey<JukeboxSong> =
        RegistryKey.of(RegistryKeys.JUKEBOX_SONG, "day_2"))

    private fun songKey(path: String): RegistryKey<JukeboxSong> =
        RegistryKey.of(RegistryKeys.JUKEBOX_SONG, "day_3"))

    // ✅ Flute of Time — still subclassed
    val FLUTE_OF_TIME: Item = register(
        "flute_of_time",
        ::TemporalTrialsItem,
        Item.Settings().maxCount(1)
    )

    // 🎵 Data-driven music discs
    val DAY_1_DISC: Item = register(
        "day_1_disc",
        { settings ->
            Item(
                settings
                    .jukeboxPlayable(songKey("day_1"))
                    .rarity(Rarity.RARE)
                    .maxCount(1)
            )
        },
        Item.Settings()
    )

    val DAY_2_DISC: Item = register(
        "day_2_disc",
        { settings ->
            Item(
                settings
                    .jukeboxPlayable(songKey("day_2"))
                    .rarity(Rarity.RARE)
                    .maxCount(1)
            )
        },
        Item.Settings()
    )

    val DAY_3_DISC: Item = register(
        "day_3_disc",
        { settings ->
            Item(
                settings
                    .jukeboxPlayable(songKey("day_3"))
                    .rarity(Rarity.RARE)
                    .maxCount(1)
            )
        },
        Item.Settings()
    )

    fun initialize() {
        FLUTE_OF_TIME
        DAY_1_DISC
        DAY_2_DISC
        DAY_3_DISC
    }
}
