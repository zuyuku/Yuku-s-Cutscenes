package zuyuku.yukuscutscenes.client.render;

import static zuyuku.yukuscutscenes.client.Client.MC;

import net.minecraft.util.Identifier;
import zuyuku.yukuscutscenes.YukusCutscenes;
import zuyuku.yukuscutscenes.client.mixin.GameRendererAccessor;
import zuyuku.yukuscutscenes.client.util.ClientScreenEffectManager;
import zuyuku.yukuscutscenes.util.LerpType;

public class ScreenEffectInstance {
    private final Identifier RESOURCE_LOCATION;
    private final int INTRO_TICKS;
    private final int HOLD_TICKS;
    private final int OUTRO_TICKS;
    private final LerpType LERP_TYPE;
    private float ageInTicks = 0f;

    public ScreenEffectInstance(String resourceLocation, int introTicks, int holdTicks, int outroTicks, String lerpType) {
        this.RESOURCE_LOCATION = Identifier.of(YukusCutscenes.MOD_ID, resourceLocation);
        this.INTRO_TICKS = introTicks;
        this.HOLD_TICKS = holdTicks;
        this.OUTRO_TICKS = outroTicks;
        this.LERP_TYPE = LerpType.fromString(lerpType);
    }

    private int totalLengthTicks() {
        return this.HOLD_TICKS + this.INTRO_TICKS + this.OUTRO_TICKS;
    }

    public void update(float tickDelta) {
        // occurs every tick
        if(this.ageInTicks < this.totalLengthTicks()) {
            this.ageInTicks+= tickDelta;
            ((GameRendererAccessor)MC.gameRenderer).invokeSetPostProcessor(this.RESOURCE_LOCATION);
            // MC.getMessageHandler().onGameMessage(Text.of("Progress: " + String.valueOf(((double)Math.round(this.getProgress()*100))/100.0)), true);
        }
        else {
            ClientScreenEffectManager.screenEffect = null;
            MC.gameRenderer.clearPostProcessor();
            MC.options.hudHidden = false;
        }
    }

    public float getProgress() {
        float progress = this.ageInTicks/(float)this.INTRO_TICKS;
        if(progress>1 && ageInTicks > this.INTRO_TICKS + this.HOLD_TICKS)
            progress = (float)(this.totalLengthTicks()-ageInTicks)/(float)this.OUTRO_TICKS;
        progress = this.LERP_TYPE.compute(Math.clamp(progress, 0, 1));
        return progress > 1 ? 1.0f : progress;
    }
}
