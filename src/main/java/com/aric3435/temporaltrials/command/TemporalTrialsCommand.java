package com.aric3435.temporaltrials.command;

import com.aric3435.temporaltrials.world.LoopManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Debug command to control Temporal Trials for the current server world.
 * Usage:
 *   /temporaltrials set <true|false>
 *   /temporaltrials toggle
 */
public final class TemporalTrialsCommand {
    private TemporalTrialsCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("temporaltrials")
                    .then(CommandManager.literal("set")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(ctx -> {
                                boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                ServerCommandSource src = ctx.getSource();
                                ServerWorld world = src.getWorld();

                                // Use the LoopManager test helper (must be public static)
                                LoopManager.setEnabledForWorld(world, enabled);

                                // sendFeedback expects a Supplier<Text> in these mappings; use a lambda
                                src.sendFeedback(() -> Text.literal("Temporal Trials set to: " + enabled), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("toggle")
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            ServerWorld world = src.getWorld();

                            // Toggle helper must exist and be public static
                            boolean newVal = LoopManager.toggleForWorld(world);

                            // sendFeedback expects a Supplier<Text>
                            src.sendFeedback(() -> Text.literal("Temporal Trials toggled to: " + newVal), true);
                            return 1;
                        })
                    )
            );
        });
    }
}