package com.aric3435.temporaltrials.item;

import com.aric3435.temporaltrials.world.LoopManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class FluteOfTimeItem extends Item {

    private static final int FLUTE_COOLDOWN_TICKS = 36000; // 1.5 in-game days

    public FluteOfTimeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!(world instanceof ServerWorld serverWorld)) {
            return ActionResult.SUCCESS;
        }

        if (!LoopManager.isTemporalTrials(serverWorld)) {
            return ActionResult.PASS;
        }

        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            user.sendMessage(Text.of("The flute’s magic hasn’t recovered yet."), true);
            return ActionResult.FAIL;
        }

        user.getItemCooldownManager().set(stack, FLUTE_COOLDOWN_TICKS);

        LoopManager.manualRewind(serverWorld, (ServerPlayerEntity) user);

        user.sendMessage(Text.of("The flute echoes through time..."), false);

        return ActionResult.SUCCESS;
    }
}
