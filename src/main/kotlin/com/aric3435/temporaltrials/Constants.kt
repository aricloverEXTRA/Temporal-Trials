package com.aric3435.temporaltrials

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.gen.WorldPreset

object Constants {
  const val MOD_ID = "temporal_trials"

  /** Networking channel ID */
  val LOOP_STATE_ID: Identifier = Identifier.of(MOD_ID, "loop_state")

  /** The RegistryKey for your data‐driven world preset */
  val TEMPORAL_TRIALS: RegistryKey<WorldPreset> =
    RegistryKey.of(RegistryKeys.WORLD_PRESET,
                   Identifier.of(MOD_ID, "temporal_trials"))
}
