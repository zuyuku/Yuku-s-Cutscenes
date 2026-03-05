package zuyuku.yukuscutscenes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import zuyuku.yukuscutscenes.client.util.ClientCutsceneManager;
import zuyuku.yukuscutscenes.client.util.ClientScreenEffectManager;

@Mixin(KeyboardInput.class)
public abstract class InputMixin extends Input {
    @Inject(method = "tick", at = @At("TAIL"))
    private void disableSprintDuringCutscene(CallbackInfo ci) {
        if (ClientCutsceneManager.inCutscene() || ClientScreenEffectManager.disableMovement()) {
            this.playerInput = PlayerInput.DEFAULT;
            this.movementVector = Vec2f.ZERO;
        }
    }
}