package zuyuku.yukuscutscenes.client.util;

import static zuyuku.yukuscutscenes.client.Client.MC;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import zuyuku.yukuscutscenes.client.render.CurveRenderer;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.CutsceneManager;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.LerpType;

@Environment(EnvType.CLIENT)
public class ClientCutsceneManager implements ClientModInitializer {
    public static final double RENDER_PLAYER_RANGE = 1.0;

    private static ArrayList<QueuedCutscene> cutsceneQueue = new ArrayList<>();
    public static Cutscene currentCutscene;

    public static float holdTimeEnd;
    public static float holdTimeStart;
    public static float currentLengthInTicks;
    public static float currentAgeInTicks;
    public static LerpType currentLerpType;
    public static Vec3d pos;
    public static Vec2f rot;

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END_MAIN.register(this::cameraUpdate);

         ClientPlayNetworking.registerGlobalReceiver(CutscenePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                boolean cancel = payload.nbt().getBoolean("Cancel", false);
                if(cancel) {
                    if(cutsceneQueue.isEmpty()) {
                        currentCutscene = null;
                        currentLengthInTicks = 0;
                        holdTimeEnd = 0;
                        holdTimeStart = 0;
                        if(!ClientScreenEffectManager.inScreenEffect())
                            MC.options.hudHidden = false;
                        return;
                    }
                    setCutscene(cutsceneQueue.getFirst());
                    return;
                }
                String name = payload.nbt().getString("PlayName", "NULLNULL");
                if(!name.matches("NULLNULL")) {
                    for(Cutscene cutscene : CurveRenderer.cutscenes)
                        if(cutscene.getName().matches(name)) {
                            Optional<UUID> uuidStart = payload.nbt().get("startPlayer", Uuids.INT_STREAM_CODEC);
                            Optional<UUID> uuidEnd = payload.nbt().get("endPlayer", Uuids.INT_STREAM_CODEC);
                            World world = MC.world;
                            if(uuidStart.isPresent())
                                cutscene = cutscene.originAtPlayer(world.getPlayerAnyDimension(uuidStart.get()));
                            if(uuidEnd.isPresent())
                                cutscene = cutscene.endAtPlayer(world.getPlayerAnyDimension(uuidEnd.get()));
                            ClientCutsceneManager.queueCutscene(cutscene, payload.nbt().getInt("Length", 0), LerpType.fromString(payload.nbt().getString("LerpType", "LINEAR")), payload.nbt().getInt("holdStart", 0), payload.nbt().getInt("holdEnd", 0));
                            return;
                        }
                }
                CurveRenderer.cutscenes = CutsceneManager.makeList(payload.nbt());
            });
        });
    }

    private void cameraUpdate(WorldRenderContext context) {
        if(!inCutscene()) 
            if(cutsceneQueue.isEmpty())
                return;
        MC.options.hudHidden = true;
        MC.options.setPerspective(Perspective.FIRST_PERSON);
        if(currentAgeInTicks >= currentLengthInTicks + holdTimeEnd + holdTimeStart) {
            if(cutsceneQueue.isEmpty()) {
                currentCutscene = null;
                if(!ClientScreenEffectManager.inScreenEffect())
                    MC.options.hudHidden = false;
                return;
            }
            setCutscene(cutsceneQueue.getFirst());
        }
        currentAgeInTicks += MC.getRenderTickCounter().getDynamicDeltaTicks();
        if(!inCutscene())
            return;
        float t = Math.clamp((currentAgeInTicks-holdTimeStart)/currentLengthInTicks, 0f, 1f);
        pos = currentCutscene.getPosAt(currentLerpType.compute(t));
        rot = currentCutscene.getRotAt(currentLerpType.compute(t));
    }

    private static void setCutscene(QueuedCutscene queuedCutscene) {
        currentCutscene = queuedCutscene.cutscene;
        currentLengthInTicks = queuedCutscene.length;
        currentLerpType = queuedCutscene.lerpType;
        holdTimeStart = queuedCutscene.holdStart;
        holdTimeEnd = queuedCutscene.holdEnd;
        currentAgeInTicks = 0f;
        cutsceneQueue.removeFirst();
    }

    public static void queueCutscene(Cutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd) {
        cutsceneQueue.add(new QueuedCutscene(cutscene, length, lerpType, holdStart, holdEnd));
    }

    public static boolean cancelCutscene(Cutscene cutscene) {
        for(QueuedCutscene queuedCutscene : cutsceneQueue)
            if(queuedCutscene.cutscene == cutscene) {
                cutsceneQueue.remove(queuedCutscene);
                return true;
            }
        return false;
    }

    public static boolean inCutscene() {
        return currentCutscene != null;
    }

    private static class QueuedCutscene {
        public Cutscene cutscene;
        public float length;
        public LerpType lerpType;
        public float holdStart;
        public float holdEnd;
        private QueuedCutscene(Cutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd) {
            this.cutscene = cutscene;
            this.length = length;
            this.lerpType = lerpType;
            this.holdStart = holdStart;
            this.holdEnd = holdEnd;
        }
    }
}
