package zuyuku.yukuscutscenes.command;

import java.util.Collection;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import zuyuku.yukuscutscenes.util.ScreenEffectPayload;
import zuyuku.yukuscutscenes.util.LerpType;
import zuyuku.yukuscutscenes.util.ScreenEffectType;

public class ScreenEffectCommand {
    public static void initialize() {
        for(ScreenEffectType instance : ScreenEffectType.values()) {
            for(LerpType lerpType : LerpType.values()) {
                LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder =
                    CommandManager.literal("screeneffect")
                    .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                    .then(
                        CommandManager.argument("players", EntityArgumentType.players())
                        .then(
                            CommandManager.literal(instance.name())
                            .then(
                                CommandManager.argument("introLength", TimeArgumentType.time(0))
                                .then(
                                    CommandManager.argument("holdLength", TimeArgumentType.time(0))
                                    .executes(context -> {
                                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                        for(ServerPlayerEntity player : players) {
                                            ScreenEffectPayload payload = new ScreenEffectPayload(instance.name(), IntegerArgumentType.getInteger(context, "introLength"), IntegerArgumentType.getInteger(context, "holdLength"), IntegerArgumentType.getInteger(context, "introLength"), LerpType.LINEAR.name());
                                            ServerPlayNetworking.send(player, payload);
                                        }
                                        context.getSource().sendFeedback(() -> Text.of("Screen effect was sent to " + String.valueOf(players.size()) + " player(s)."), false);
                                        return players.size();
                                    })
                                    .then(
                                        CommandManager.argument("outroLength", TimeArgumentType.time(0))
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                            for(ServerPlayerEntity player : players) {
                                                ScreenEffectPayload payload = new ScreenEffectPayload(instance.name(), IntegerArgumentType.getInteger(context, "introLength"), IntegerArgumentType.getInteger(context, "holdLength"), IntegerArgumentType.getInteger(context, "outroLength"), LerpType.LINEAR.name());
                                                ServerPlayNetworking.send(player, payload);
                                            }
                                            context.getSource().sendFeedback(() -> Text.of("Screen effect was sent to " + String.valueOf(players.size()) + " player(s)."), false);
                                            return players.size();
                                        })
                                        .then(
                                            CommandManager.literal(lerpType.name())
                                            .executes(context -> {
                                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                for(ServerPlayerEntity player : players) {
                                                    ScreenEffectPayload payload = new ScreenEffectPayload(instance.name(), IntegerArgumentType.getInteger(context, "introLength"), IntegerArgumentType.getInteger(context, "holdLength"), IntegerArgumentType.getInteger(context, "outroLength"), lerpType.name());
                                                    ServerPlayNetworking.send(player, payload);
                                                }
                                                context.getSource().sendFeedback(() -> Text.of("Screen effect was sent to " + String.valueOf(players.size()) + " player(s)."), false);
                                                return players.size();
                                            })
                                        )
                                    )
                                    .then(
                                        CommandManager.literal(lerpType.name())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                            for(ServerPlayerEntity player : players) {
                                                ScreenEffectPayload payload = new ScreenEffectPayload(instance.name(), IntegerArgumentType.getInteger(context, "introLength"), IntegerArgumentType.getInteger(context, "holdLength"), IntegerArgumentType.getInteger(context, "introLength"), lerpType.name());
                                                ServerPlayNetworking.send(player, payload);
                                            }
                                            context.getSource().sendFeedback(() -> Text.of("Screen effect was sent to " + String.valueOf(players.size()) + " player(s)."), false);
                                            return players.size();
                                        })
                                    )
                                )
                            )
                        )
                    );
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literalArgumentBuilder));
            }
        }
    }
}
