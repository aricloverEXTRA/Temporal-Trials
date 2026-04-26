package com.aric3435.temporaltrials.command;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import com.aric3435.temporaltrials.player.PlayerDataComponent;
import com.aric3435.temporaltrials.player.PlayerDataProvider;
import com.aric3435.temporaltrials.world.LoopManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Debug command to control Temporal Trials for the current server world.
 * Usage:
 *   /temporaltrials set <true|false>
 *   /temporaltrials toggle
 *   /temporaltrials help
 *
 * Lives subcommands (only if TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED):
 *   /temporaltrials lives add <player> <amount>
 *   /temporaltrials lives set <player> <amount>
 *   /temporaltrials lives all <amount>
 */
public final class TemporalTrialsCommand {
    private TemporalTrialsCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = CommandManager.literal("temporaltrials")
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                            ServerCommandSource src = ctx.getSource();
                            ServerWorld world = src.getWorld();

                            LoopManager.setEnabledForWorld(world, enabled);
                            src.sendFeedback(() -> Text.literal("Temporal Trials set to: " + enabled), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("toggle")
                    .executes(ctx -> {
                        ServerCommandSource src = ctx.getSource();
                        ServerWorld world = src.getWorld();

                        boolean newVal = LoopManager.toggleForWorld(world);
                        src.sendFeedback(() -> Text.literal("Temporal Trials toggled to: " + newVal), true);
                        return 1;
                    })
                )
                .then(CommandManager.literal("help")
                    .executes(ctx -> {
                        ServerCommandSource src = ctx.getSource();
                        MinecraftServer server = src.getServer();
                        src.sendFeedback(() -> Text.literal(TemporalTrialsConfig.getHelpMessage(server)), false);
                        return 1;
                    })
                );

            // Register lives command only if the feature is enabled in config
            if (TemporalTrialsConfig.MULTIPLAYER_LIVES_ENABLED) {
                var livesNode = CommandManager.literal("lives")
                        .requires(src -> src.hasPermissionLevel(2)); // ops only

                // /temporaltrials lives add <player> <amount>
                livesNode = livesNode.then(CommandManager.literal("add")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 999))
                                        .executes(ctx -> {
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            ServerCommandSource src = ctx.getSource();
                                            MinecraftServer server = src.getServer();

                                            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
                                            if (player == null) {
                                                src.sendFeedback(() -> Text.literal("§cCould not find player: " + playerName), false);
                                                return 0;
                                            }

                                            PlayerDataComponent pData = PlayerDataProvider.get(player);
                                            int newLives = pData.getRemainingLives() + amount;
                                            pData.setRemainingLives(newLives);

                                            src.sendFeedback(() -> Text.literal(
                                                    "§aAdded " + amount + " lives to " + playerName + " (" + newLives + " total)"), true);
                                            player.sendMessage(Text.literal(
                                                    "§eYou received " + amount + " extra lives (now " + newLives + ")!"), false);
                                            return 1;
                                        })
                                )
                        )
                );

                // /temporaltrials lives set <player> <amount>
                livesNode = livesNode.then(CommandManager.literal("set")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 999))
                                        .executes(ctx -> {
                                            String playerName = StringArgumentType.getString(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            ServerCommandSource src = ctx.getSource();
                                            MinecraftServer server = src.getServer();

                                            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
                                            if (player == null) {
                                                src.sendFeedback(() -> Text.literal("§cCould not find player: " + playerName), false);
                                                return 0;
                                            }

                                            PlayerDataComponent pData = PlayerDataProvider.get(player);
                                            pData.setRemainingLives(amount);

                                            src.sendFeedback(() -> Text.literal(
                                                    "§aSet lives for " + playerName + " to " + amount + "."), true);
                                            player.sendMessage(Text.literal(
                                                    "§eYour Temporal Trials lives have been set to: " + amount), false);
                                            return 1;
                                        })
                                )
                        )
                );

                // /temporaltrials lives all <amount>
                livesNode = livesNode.then(CommandManager.literal("all")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 999))
                                .executes(ctx -> {
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    ServerCommandSource src = ctx.getSource();
                                    MinecraftServer server = src.getServer();

                                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                        PlayerDataComponent pData = PlayerDataProvider.get(player);
                                        pData.setRemainingLives(amount);
                                        player.sendMessage(Text.literal(
                                                "§eYour Temporal Trials lives have been set to: " + amount), false);
                                    }
                                    src.sendFeedback(() -> Text.literal(
                                            "§aSet all players' lives to " + amount), true);
                                    return 1;
                                })
                        )
                );

                root = root.then(livesNode);
            }

            dispatcher.register(root);
        });
    }
}