package com.aric3435.temporaltrials.item;

import com.aric3435.temporaltrials.world.LoopManager;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class FluteOfTimeItem extends Item {

    // 1.5 in-game days cooldown (36000 ticks)
    private static final int FLUTE_COOLDOWN_TICKS = 36000;
    
    // Server-wide cooldown (global for all players)
    private static long globalFluteLastUseTick = -1L;

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
            user.sendMessage(Text.of("§6The flute only works in Temporal Trials..."), true);
            return ActionResult.PASS;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) user;
        MinecraftServer server = serverWorld.getServer();

        // Check global server-wide cooldown
        long currentServerTick = server.getTicks();
        long timeSinceLastUse = currentServerTick - globalFluteLastUseTick;

        if (globalFluteLastUseTick != -1L && timeSinceLastUse < FLUTE_COOLDOWN_TICKS) {
            long ticksRemaining = FLUTE_COOLDOWN_TICKS - timeSinceLastUse;
            double minutesRemaining = ticksRemaining / 1200.0; // 20 ticks per second, 60 seconds per minute
            
            user.sendMessage(
                Text.of("§c§lThe flute's magic is still recovering..."),
                true
            );
            user.sendMessage(
                Text.of(String.format("§6Time remaining: §e%.1f minutes", minutesRemaining)),
                true
            );
            
            System.out.println("[TemporalTrials] Player " + player.getName().getString() + 
                             " tried to use flute but it's on cooldown (" + ticksRemaining + " ticks remaining)");
            
            return ActionResult.FAIL;
        }

        // **SET THE GLOBAL COOLDOWN FOR ALL PLAYERS**
        globalFluteLastUseTick = currentServerTick;
        System.out.println("[TemporalTrials] Flute cooldown reset to global tick " + currentServerTick);

        // **SAVE CURRENT INVENTORY BEFORE RESET**
        // In Hard Mode: Saves this cycle's inventory for next cycle (but risky if you die)
        // In Easy/Normal: Saves this cycle's inventory as safety net
        PlayerDataProvider.get(player).saveInventory(player.getInventory().main);
        
        System.out.println("[TemporalTrials] Player " + player.getName().getString() + 
                         " used Flute - inventory saved for next cycle");

        // Announce to all players
        for (ServerPlayerEntity p : serverWorld.getPlayers()) {
            p.sendMessage(
                Text.of("§b§l✦ " + player.getName().getString() + " played the Flute of Time! ✦"),
                false
            );
        }

        // **TRIGGER WORLD RESET**
        LoopManager.resetLoop(serverWorld);

        user.sendMessage(Text.of("§b§l╔════════════════════════════════════╗"), false);
        user.sendMessage(Text.of("§b§l║  The flute echoes through time...  ║"), false);
        user.sendMessage(Text.of("§b§l║  The world resets to the First Day. ║"), false);
        user.sendMessage(Text.of("§b§l║  Your treasures are preserved.      ║"), false);
        user.sendMessage(Text.of("§b§l╚════════════════════════════════════╝"), false);

        return ActionResult.SUCCESS;
    }

    /**
     * Get remaining cooldown ticks on the Flute of Time (server-wide)
     */
    public static long getRemainingCooldownTicks(MinecraftServer server) {
        if (globalFluteLastUseTick == -1L) {
            return 0;
        }
        long timeSinceLastUse = server.getTicks() - globalFluteLastUseTick;
        return Math.max(0, FLUTE_COOLDOWN_TICKS - timeSinceLastUse);
    }

    /**
     * Reset the global cooldown (for debug/admin purposes)
     */
    public static void resetGlobalCooldown() {
        globalFluteLastUseTick = -1L;
        System.out.println("[TemporalTrials] Flute cooldown manually reset");
    }
}