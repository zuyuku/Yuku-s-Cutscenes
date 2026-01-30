package zuyuku.yukuscutscenes.client.util;

import static zuyuku.yukuscutscenes.client.Client.MC;

import java.util.ArrayList;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.LerpType;

@Environment(EnvType.CLIENT)
public class ClientCutsceneManager implements ClientModInitializer{
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
                MC.options.hudHidden = false;
                return;
            }
            setCutscene(cutsceneQueue.getFirst());
        }
        currentAgeInTicks += MC.getRenderTickCounter().getDynamicDeltaTicks();
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

    public static void queueCutsceneEntityOrigin(Cutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd, PlayerEntity player) {
        cutsceneQueue.add(new QueuedCutscene(cutscene.originAtPlayer(player), length, lerpType, holdStart, holdEnd));
    }

    public static void queueCutsceneEntityEnd(Cutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd, PlayerEntity player) {
        cutsceneQueue.add(new QueuedCutscene(cutscene.endAtPlayer(player), length, lerpType, holdStart, holdEnd));
    }

    public static void queueCutsceneEntityOriginAndEnd(Cutscene cutscene, float length, LerpType lerpType, float holdStart, float holdEnd, PlayerEntity player, PlayerEntity endPlayer) {
        cutsceneQueue.add(new QueuedCutscene(cutscene.endAtPlayer(endPlayer).originAtPlayer(player), length, lerpType, holdStart, holdEnd));
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
