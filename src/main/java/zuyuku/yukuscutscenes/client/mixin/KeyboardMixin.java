package zuyuku.yukuscutscenes.client.mixin;

import static zuyuku.yukuscutscenes.client.Client.MC;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import zuyuku.yukuscutscenes.client.util.ClientCutsceneManager;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at=@At("head"), cancellable = true)
    public void disableCutsceneMovement(long window, @KeyInput.KeyAction int action, KeyInput input, CallbackInfo info) {
        if(ClientCutsceneManager.inCutscene() && MC.currentScreen == null && input.key() != GLFW.GLFW_KEY_ESCAPE && !MC.options.fullscreenKey.matchesKey(input)) {
            KeyBinding.unpressAll();
            info.cancel();
        }
    }
}
