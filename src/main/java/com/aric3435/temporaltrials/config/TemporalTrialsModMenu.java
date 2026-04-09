package com.aric3435.temporaltrials.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.aric3435.temporaltrials.client.config.TemporalTrialsConfigScreen;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.gui.screen.Screen;

/**
 * Mod Menu integration - CLIENT-SIDE ONLY
 * Allows players to open client config GUI from Mod Menu
 */
@Environment(EnvType.CLIENT)
public class TemporalTrialsModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> TemporalTrialsConfigScreen.createConfigScreen(parent);
    }
}