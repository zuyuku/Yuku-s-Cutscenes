package zuyuku.yukuscutscenes.client.util;

import static zuyuku.yukuscutscenes.client.Client.MC;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import zuyuku.yukuscutscenes.client.render.ScreenEffectInstance;
import zuyuku.yukuscutscenes.util.ScreenEffectPayload;

public class ClientScreenEffectManager implements ClientModInitializer {

    public static ScreenEffectInstance screenEffect;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenEffectPayload.ID, (payload, context) -> {
            screenEffect = new ScreenEffectInstance(payload.name(), payload.introTicks(), payload.holdTicks(), payload.outroTicks(), payload.lerpType(), payload.command());
            MC.options.hudHidden = true;
        });

        WorldRenderEvents.END_MAIN.register(client -> {
            if(inScreenEffect())
                screenEffect.update(MC.getRenderTickCounter().getDynamicDeltaTicks());
        });
    }

    public static boolean inScreenEffect() {
        return screenEffect != null;
    }

    public static boolean disableMovement() {
        return !inScreenEffect() ? false : !screenEffect.canPlayerMove();
    }
}
