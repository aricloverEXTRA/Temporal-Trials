package com.aric3435.temporaltrials

import net.minecraft.item.Item
import net.minecraft.resource.featuretoggle.FeatureSet

/**
 * Base class for all Temporal Trials items.
 * Always enabled so it shows up in inventories.
 */
open class TemporalTrialsItem(settings: Settings) : Item(settings) {
  override fun isEnabled(enabledFeatures: FeatureSet): Boolean = true
}
