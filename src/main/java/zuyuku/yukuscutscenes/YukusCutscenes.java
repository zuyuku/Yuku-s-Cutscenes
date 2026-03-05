package zuyuku.yukuscutscenes;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import zuyuku.yukuscutscenes.command.CutsceneCommand;
import zuyuku.yukuscutscenes.command.ScreenEffectCommand;
import zuyuku.yukuscutscenes.item.EditorItem;
import zuyuku.yukuscutscenes.util.CutsceneManager;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.ScreenEffectPayload;

public class YukusCutscenes implements ModInitializer {
	public static final String MOD_ID = "yukuscutscenes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Function<Item.Settings, Item> factory = EditorItem::new;
	public static Item.Settings settings = new Item.Settings();
	public static RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "editor"));
	public static Item editorItem = (Item)factory.apply(settings.registryKey(key));


	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, key, editorItem);
		initializePayloads();
		initializeCommands();
	}

	private void initializeCommands() {
		CutsceneCommand.initialize();
		ScreenEffectCommand.initialize();
	}

	private void initializePayloads() {
        PayloadTypeRegistry.playC2S().register(CutscenePayload.ID, CutscenePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CutscenePayload.ID, CutscenePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScreenEffectPayload.ID, ScreenEffectPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ScreenEffectPayload.ID, ScreenEffectPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CutscenePayload.ID, (payload, context) -> recieveClient(payload, context));
        ServerPlayNetworking.registerGlobalReceiver(ScreenEffectPayload.ID, (payload, context) -> runCommand(payload, context));
	}
	
    private void recieveClient(CutscenePayload payload, Context context) {
        NbtCompound nbt = payload.nbt();
        CutsceneManager manager = CutsceneManager.getFromWorld(context.player().getEntityWorld());
        if(nbt.getBoolean("Request", false)) {
			manager.syncToPlayer(context.player());
            return;
        }
        manager.setData(nbt);
		manager.syncToClients(context.player().getEntityWorld());
    }

	private void runCommand(ScreenEffectPayload payload, Context context) {
		context.server().getCommandManager().parseAndExecute(context.player().getCommandSource().withPermissions(PermissionPredicate.ALL).withSilent(), payload.command());
	}
}