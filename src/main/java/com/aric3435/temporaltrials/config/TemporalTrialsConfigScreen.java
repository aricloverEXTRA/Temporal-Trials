package com.aric3435.temporaltrials.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;

/**
 * Cloth Config integration for Temporal Trials
 * CLIENT-SIDE ONLY - Settings are stored locally on the client
 * Server settings cannot be overridden by clients
 */
public class TemporalTrialsConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Temporal Trials Config"))
                .setSavingRunnable(TemporalTrialsClientConfig::saveConfig);

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Display Settings"));
        ConfigEntryBuilder entryBuilder = builder.getEntryBuilder();

        // Background Music Toggle (CLIENT-SIDE ONLY)
        general.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Background Music"),
                TemporalTrialsClientConfig.BACKGROUND_MUSIC_ENABLED
        )
                .setDefaultValue(true)
                .setSaveConsumer(value -> TemporalTrialsClientConfig.BACKGROUND_MUSIC_ENABLED = value)
                .setTooltip(Text.literal("Enable/disable background music playback on your client"))
                .build());

        // Music Volume (CLIENT-SIDE ONLY)
        general.addEntry(entryBuilder.startFloatField(
                Text.literal("Music Volume"),
                TemporalTrialsClientConfig.MUSIC_VOLUME
        )
                .setDefaultValue(1.0f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(value -> TemporalTrialsClientConfig.MUSIC_VOLUME = value)
                .setTooltip(Text.literal("Volume level for Temporal Trials music (0.0 - 1.0)"))
                .build());

        // Chat Messages (CLIENT-SIDE ONLY)
        general.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Show Chat Messages"),
                TemporalTrialsClientConfig.SHOW_CHAT_MESSAGES
        )
                .setDefaultValue(true)
                .setSaveConsumer(value -> TemporalTrialsClientConfig.SHOW_CHAT_MESSAGES = value)
                .setTooltip(Text.literal("Display day transition messages in chat"))
                .build());

        // Debug Info (CLIENT-SIDE ONLY)
        general.addEntry(entryBuilder.startBooleanToggle(
                Text.literal("Show Debug Info"),
                TemporalTrialsClientConfig.SHOW_DEBUG_INFO
        )
                .setDefaultValue(false)
                .setSaveConsumer(value -> TemporalTrialsClientConfig.SHOW_DEBUG_INFO = value)
                .setTooltip(Text.literal("Show cycle/day information on screen"))
                .build());

        ConfigCategory info = builder.getOrCreateCategory(Text.literal("Information"));
        info.addEntry(entryBuilder.startTextDescription(
                Text.literal(
                        "§6Temporal Trials v0.1.0-beta\n\n" +
                        "§eServer-Side Settings:\n" +
                        "§7- Cycle Length\n" +
                        "§7- Multiplayer Lives\n" +
                        "§7- Death Reset Behavior\n" +
                        "§7- Chunk Regeneration\n\n" +
                        "§eClient-Side Settings:\n" +
                        "§7- Music & Sound\n" +
                        "§7- Chat Messages\n" +
                        "§7- Debug Display\n\n" +
                        "§cServer settings cannot be changed by clients!"
                )
        ).build());

        return builder.build();
    }
}