package zuyuku.yukuscutscenes.client.mixin;

import static zuyuku.yukuscutscenes.client.Client.MC;

import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.input.Scroller;
import zuyuku.yukuscutscenes.client.render.CurveRenderer;
import zuyuku.yukuscutscenes.client.util.ClientCutsceneManager;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow
    protected Scroller scroller;

    
    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void disableCutsceneInteract(long window, MouseInput input, @MouseInput.MouseAction int action, CallbackInfo info) {
        if(ClientCutsceneManager.inCutscene() && MC.currentScreen == null)
            info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
    private void editorScrolling(long window, double horizontal, double vertical, CallbackInfo info) {
        if(this.scroller == null)
            return;
        if(ClientCutsceneManager.inCutscene() && MC.currentScreen == null)
            info.cancel();
        if(CurveRenderer.storedPoint != null) {
			boolean bl = MC.options.getDiscreteMouseScroll().getValue();
			double d = MC.options.getMouseWheelSensitivity().getValue();
			double e = (bl ? Math.signum(horizontal) : horizontal) * d;
			double f = (bl ? Math.signum(vertical) : vertical) * d;
			Vector2i vector2i = this.scroller.update(e, f);
			if (vector2i.x == 0 && vector2i.y == 0)
				return;
            int i = vector2i.y == 0 ? -vector2i.x : vector2i.y;
            CurveRenderer.updateStoredDistance(i);
            info.cancel();
        }
    }

    @Inject(method = "updateMouse", at=@At("HEAD"), cancellable = true)
    public void disableCutsceneLook(CallbackInfo info) {
        if(ClientCutsceneManager.inCutscene() && MC.currentScreen == null) 
            info.cancel();
    }
}
