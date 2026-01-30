package zuyuku.yukuscutscenes.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    public static MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
    }
}
