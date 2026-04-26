package com.aric3435.temporaltrials.item;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Simple disk item fallback: plays its SoundEvent when used (right-click).
 * This ensures discs play even if jukebox-song registration/datagen isn't present.
 */
public class TemporalMusicDiscItem extends Item {
    private final SoundEvent sound;

    public TemporalMusicDiscItem(SoundEvent sound, Settings settings) {
        super(settings);
        this.sound = sound;
    }

    @Override
    public ActionResult use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        if (world.isClient) {
            // Client-side no-op (server triggers actual sound)
            return ActionResult.SUCCESS;
        }

        if (user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSound(sound, 1.0f, 1.0f);
        }

        return ActionResult.SUCCESS;
    }
}