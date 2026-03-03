package zuyuku.yukuscutscenes.client.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.util.Handle;
import net.minecraft.util.Identifier;
import zuyuku.yukuscutscenes.client.util.ClientScreenEffectManager;
import zuyuku.yukuscutscenes.util.ScreenEffectType;


@Mixin(PostEffectPass.class)
public abstract class PostEffectPassMixin {

    @Shadow
    private Map<String, GpuBuffer> uniformBuffers;

    @Shadow
    private String id;  

    private GpuBuffer progressBuffer;

    @Inject(method = "render", at = @At("HEAD"))
    private void updateProgress(
            FrameGraphBuilder builder,
            Map<Identifier, Handle<Framebuffer>> handles,
            GpuBufferSlice slice,
            CallbackInfo ci
    ) {
        boolean notCustom = true;
        for(ScreenEffectType type : ScreenEffectType.values())
            if(id.contains(type.name())) {
                notCustom = false;
                break;
            }
        if (notCustom) return;

        // Create once
        if (progressBuffer == null) {
            progressBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Progress",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                    16
            );

        }
        uniformBuffers.put("ProgressBuffer", progressBuffer);

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView view =
                encoder.mapBuffer(progressBuffer, false, true)) {

            Std140Builder std140 = Std140Builder.intoBuffer(view.data());
            std140.putFloat(ClientScreenEffectManager.screenEffect.getProgress());
        }
    }
}
