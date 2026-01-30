package zuyuku.yukuscutscenes.client.mixin;

import static zuyuku.yukuscutscenes.client.Client.MC;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import zuyuku.yukuscutscenes.client.util.ClientCutsceneManager;

@Mixin(Camera.class)
public class CameraMixin {
    @Shadow
    private Vec3d pos;
    @Shadow
	private final Quaternionf rotation = new Quaternionf();
    @Shadow
	private float pitch;
    @Shadow
	private float yaw;

    @Inject(at = @At("HEAD"), method = "isThirdPerson", cancellable = true)
    public void thirdPersonIfInCutscene(CallbackInfoReturnable<Boolean> info) {
        if(ClientCutsceneManager.inCutscene())
            if(!MC.player.getEyePos().isInRange(new Vec3d(ClientCutsceneManager.pos.x, ClientCutsceneManager.pos.y, ClientCutsceneManager.pos.z), ClientCutsceneManager.RENDER_PLAYER_RANGE))
                info.setReturnValue(true);
    }

    @Inject(at = @At("TAIL"), method = "setPos")
    protected void setPos(CallbackInfo info) {
        if(!ClientCutsceneManager.inCutscene())
            return;
        this.pos = ClientCutsceneManager.pos;
        this.pitch = ClientCutsceneManager.rot.x;
        this.yaw = ClientCutsceneManager.rot.y;
        this.rotation.rotationYXZ((float) Math.PI - this.yaw * (float) (Math.PI / 180.0), -this.pitch * (float) (Math.PI / 180.0), 0.0F);
    }
}
