package zuyuku.yukuscutscenes.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.CutsceneManager;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.LerpType;
import zuyuku.yukuscutscenes.util.bezier.BezierPath;

public class CutsceneCommand implements ModInitializer {
    public static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestIdentifiers(
		CutsceneManager.getFromWorld(context.getSource().getWorld()).getSuggestions(), builder
	);

    @Override
    public void onInitialize() {
        List<List<String>> holdTypes = List.of(List.of("holdStart", "holdEnd"), List.of("holdEnd", "holdStart"));
        List<List<String>> anchorTypes = List.of(List.of("startPlayer", "endPlayer"), List.of("endPlayer", "startPlayer"));
        for(List<String> anchorType : anchorTypes) {
            for(List<String> holdType : holdTypes) {
                for(LerpType lerpType : LerpType.values()) {
                    LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = 
                        CommandManager.literal("cutscene")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(
                            CommandManager.literal("resetAll")
                            .then(
                                CommandManager.literal("confirm")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    CutsceneManager manager = CutsceneManager.getFromWorld(source.getWorld());
                                    manager.setData(new NbtCompound());
                                    manager.syncToClients(source.getWorld());
                                    source.sendFeedback(() -> Text.literal("All cutscenes have been removed from world."), true);
                                    return 1;
                                })
                            )
                        )
                        .then(
                            CommandManager.literal("add")
                            .then(
                                CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> addCutscene(context.getSource(), StringArgumentType.getString(context, "name")))
                            )
                        )
                        .then(
                            CommandManager.literal("list")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ArrayList<Cutscene> cutscenes = CutsceneManager.getFromWorld(source.getWorld()).getCutscenes();
                                source.sendFeedback(() -> Text.literal("There are [" + String.valueOf(cutscenes.size()) + "] cutscenes:").formatted(Formatting.YELLOW), false);
                                for(Cutscene cutscene : cutscenes)
                                    source.sendFeedback(() -> Text.literal("     " + cutscene.getName()), false);
                                return 1;
                            })
                        )
                        .then(
                            CommandManager.literal("remove")
                            .then(
                                CommandManager.argument("name", IdentifierArgumentType.identifier())
                                .suggests(SUGGESTION_PROVIDER)
                                .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                    String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                    CutsceneManager manager = CutsceneManager.getFromWorld(source.getWorld());
                                    ArrayList<Cutscene> cutscenes = manager.getCutscenes();
                                    for(Cutscene cutscene : cutscenes) {
                                        if(cutscene.getName().matches(name)) {
                                            cutscenes.remove(cutscene);
                                            manager.setList(cutscenes);
                                            source.sendFeedback(() -> Text.literal("Cutscene [" + name + "] was removed."), true);
                                            manager.syncToClients(source.getWorld());
                                            return 1;
                                        }
                                    }
                                    source.sendError(Text.of("No cutscene of name [" + name + "] could be found."));
                                    return 0;
                                })
                            )
                        )
                        .then(
                            CommandManager.literal("play")
                            .then(
                                CommandManager.argument("player", EntityArgumentType.player())
                                .then(
                                    CommandManager.argument("name", IdentifierArgumentType.identifier())
                                    .suggests(SUGGESTION_PROVIDER)
                                    .then(
                                        CommandManager.argument("length", TimeArgumentType.time(0))
                                        .then(
                                            CommandManager.literal(lerpType.name())
                                            .executes(context -> {
                                                boolean cont = false;
                                                String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                                CutsceneManager manager = CutsceneManager.getFromWorld(context.getSource().getWorld());
                                                for(Cutscene cutscene2 : manager.getCutscenes())
                                                    if(name.matches(cutscene2.getName()))
                                                        cont = true;
                                                if(!cont) {
                                                    context.getSource().sendError(Text.of("No cutscene found with that name."));
                                                    return 0;
                                                }
                                                NbtCompound nbt = new NbtCompound();
                                                nbt.putString("PlayName", name);
                                                nbt.putString("LerpType", lerpType.name());
                                                nbt.putInt("Length", IntegerArgumentType.getInteger(context, "length"));
                                                manager.syncToPlayer(EntityArgumentType.getPlayer(context, "player"));
                                                ServerPlayNetworking.send(EntityArgumentType.getPlayer(context, "player"), new CutscenePayload(nbt));
                                                return 1;
                                            })
                                            .then(
                                                CommandManager.literal(holdType.get(0))
                                                .then(
                                                    CommandManager.argument(holdType.get(0), TimeArgumentType.time(0))
                                                    .executes(context -> {
                                                        boolean cont = false;
                                                        String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                                        CutsceneManager manager = CutsceneManager.getFromWorld(context.getSource().getWorld());
                                                        for(Cutscene cutscene2 : manager.getCutscenes())
                                                            if(name.matches(cutscene2.getName()))
                                                                cont = true;
                                                        if(!cont) {
                                                            context.getSource().sendError(Text.of("No cutscene found with that name."));
                                                            return 0;
                                                        }
                                                        NbtCompound nbt = new NbtCompound();
                                                        nbt.putString("PlayName", name);
                                                        nbt.putString("LerpType", lerpType.name());
                                                        nbt.putInt("Length", IntegerArgumentType.getInteger(context, "length"));
                                                        nbt.putInt(holdType.get(0), IntegerArgumentType.getInteger(context, holdType.get(0)));
                                                        manager.syncToPlayer(EntityArgumentType.getPlayer(context, "player"));
                                                        ServerPlayNetworking.send(EntityArgumentType.getPlayer(context, "player"), new CutscenePayload(nbt));
                                                        return 1;
                                                    })
                                                    .then(
                                                        CommandManager.literal(holdType.get(1))
                                                        .then(
                                                            CommandManager.argument(holdType.get(1), TimeArgumentType.time(0))
                                                            .executes(context -> {
                                                                boolean cont = false;
                                                                String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                                                CutsceneManager manager = CutsceneManager.getFromWorld(context.getSource().getWorld());
                                                                for(Cutscene cutscene2 : manager.getCutscenes())
                                                                    if(name.matches(cutscene2.getName()))
                                                                        cont = true;
                                                                if(!cont) {
                                                                    context.getSource().sendError(Text.of("No cutscene found with that name."));
                                                                    return 0;
                                                                }
                                                                NbtCompound nbt = new NbtCompound();
                                                                nbt.putString("PlayName", name);
                                                                nbt.putString("LerpType", lerpType.name());
                                                                nbt.putInt("Length", IntegerArgumentType.getInteger(context, "length"));
                                                                nbt.putInt(holdType.get(0), IntegerArgumentType.getInteger(context, holdType.get(0)));
                                                                nbt.putInt(holdType.get(1), IntegerArgumentType.getInteger(context, holdType.get(1)));
                                                                manager.syncToPlayer(EntityArgumentType.getPlayer(context, "player"));
                                                                ServerPlayNetworking.send(EntityArgumentType.getPlayer(context, "player"), new CutscenePayload(nbt));
                                                                return 1;
                                                            })
                                                            .then(
                                                                CommandManager.literal(anchorType.get(0))
                                                                .executes(context -> {
                                                                    boolean cont = false;
                                                                    String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                                                    CutsceneManager manager = CutsceneManager.getFromWorld(context.getSource().getWorld());
                                                                    for(Cutscene cutscene2 : manager.getCutscenes())
                                                                        if(name.matches(cutscene2.getName()))
                                                                            cont = true;
                                                                    if(!cont) {
                                                                        context.getSource().sendError(Text.of("No cutscene found with that name."));
                                                                        return 0;
                                                                    }
                                                                    NbtCompound nbt = new NbtCompound();
                                                                    UUID playerUuid = EntityArgumentType.getPlayer(context, "player").getUuid();
                                                                    nbt.putString("PlayName", name);
                                                                    nbt.putString("LerpType", lerpType.name());
                                                                    nbt.putInt("Length", IntegerArgumentType.getInteger(context, "length"));
                                                                    nbt.putInt(holdType.get(0), IntegerArgumentType.getInteger(context, holdType.get(0)));
                                                                    nbt.putInt(holdType.get(1), IntegerArgumentType.getInteger(context, holdType.get(1)));
                                                                    nbt.put(anchorType.get(0), Uuids.INT_STREAM_CODEC, playerUuid);
                                                                    manager.syncToPlayer(EntityArgumentType.getPlayer(context, "player"));
                                                                    ServerPlayNetworking.send(EntityArgumentType.getPlayer(context, "player"), new CutscenePayload(nbt));
                                                                    return 1;
                                                                })
                                                                .then(
                                                                    CommandManager.literal(anchorType.get(1))
                                                                    .executes(context -> {
                                                                        boolean cont = false;
                                                                        String name = IdentifierArgumentType.getIdentifier(context, "name").getPath();
                                                                        CutsceneManager manager = CutsceneManager.getFromWorld(context.getSource().getWorld());
                                                                        for(Cutscene cutscene2 : manager.getCutscenes())
                                                                            if(name.matches(cutscene2.getName()))
                                                                                cont = true;
                                                                        if(!cont) {
                                                                            context.getSource().sendError(Text.of("No cutscene found with that name."));
                                                                            return 0;
                                                                        }
                                                                        NbtCompound nbt = new NbtCompound();
                                                                        UUID playerUuid = EntityArgumentType.getPlayer(context, "player").getUuid();
                                                                        nbt.putString("PlayName", name);
                                                                        nbt.putString("LerpType", lerpType.name());
                                                                        nbt.putInt("Length", IntegerArgumentType.getInteger(context, "length"));
                                                                        nbt.putInt(holdType.get(0), IntegerArgumentType.getInteger(context, holdType.get(0)));
                                                                        nbt.putInt(holdType.get(1), IntegerArgumentType.getInteger(context, holdType.get(1)));
                                                                        nbt.put(anchorType.get(0), Uuids.INT_STREAM_CODEC, playerUuid);
                                                                        nbt.put(anchorType.get(1), Uuids.INT_STREAM_CODEC, playerUuid);
                                                                        manager.syncToPlayer(EntityArgumentType.getPlayer(context, "player"));
                                                                        ServerPlayNetworking.send(EntityArgumentType.getPlayer(context, "player"), new CutscenePayload(nbt));
                                                                        return 1;
                                                                    })
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
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

    private int addCutscene(ServerCommandSource source, String name) {
        CutsceneManager manager = CutsceneManager.getFromWorld(source.getWorld());
        ArrayList<Cutscene> cutscenes = manager.getCutscenes();
        for(Cutscene cutscene : cutscenes)
            if(cutscene.getName().matches(name)) {
                source.sendError(Text.of("Cutscene of name [" + name + "] already exists!"));
                return 0;
            }
        BezierPath path = new BezierPath(source.getPlayer().getEyePos());
        cutscenes.add(new Cutscene(name, source.getPlayer().getRotationClient(), source.getPlayer().getRotationClient(), path));
        manager.setList(cutscenes);
        manager.syncToClients(source.getWorld());
        return 1;
    }
}
