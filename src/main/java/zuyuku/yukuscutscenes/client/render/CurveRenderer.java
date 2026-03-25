package zuyuku.yukuscutscenes.client.render;

import static zuyuku.yukuscutscenes.client.Client.MC;

import java.util.ArrayList;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zuyuku.yukuscutscenes.YukusCutscenes;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.bezier.BezierPoint;

public class CurveRenderer implements ClientModInitializer {
    public static ArrayList<Cutscene> cutscenes = new ArrayList<>();
    public static BezierPoint storedPoint;
    public static double storedDistance;

    private void renderBeziers(WorldRenderContext context) {
        if(MC.player.isHolding(YukusCutscenes.editorItem)){
            for(Cutscene cutscene : cutscenes)
                cutscene.render(context);
            if(storedPoint != null) {
                MC.player.sendMessage(Text.of("Scroll to push/pull points.    Current distance from camera: " + String.valueOf(storedDistance)), true);
                storedPoint.setPos(calculateNewPoint(MC.player));
                for(Cutscene cutscene : cutscenes)
                    for(BezierPoint nearPoint : cutscene.path.getPoints())
                        if(nearPoint != storedPoint)
                            if(nearPoint.getPos().distanceTo(storedPoint.getPos()) <= 0.1) {
                                storedPoint.setPos(nearPoint.getPos());
                                if(storedPoint.isEnd() && nearPoint.isEnd()) {
                                    Vec2f rot = nearPoint.getPath().getClientCutscene().getRotAt(1);
                                    if(nearPoint.isFirst())
                                        rot = nearPoint.getPath().getClientCutscene().getRotAt(0);
                                    if(storedPoint.isFirst())
                                        storedPoint.getPath().getClientCutscene().setInitRot(rot);
                                    else if(storedPoint.isLast())
                                        storedPoint.getPath().getClientCutscene().setFinalRot(rot);
                                }
                                return;
                            }
                if(storedPoint.isFirst())
                    storedPoint.getPath().getClientCutscene().setInitRot(MC.player.getRotationClient());
                else if(storedPoint.isLast())
                    storedPoint.getPath().getClientCutscene().setFinalRot(MC.player.getRotationClient());
            }
        }
	}

    public static void updateStoredDistance(double i) {
        storedDistance = Math.max(storedDistance + (i*0.25), 0);
    }

    private Vec3d calculateNewPoint(LivingEntity user) {
        double rotation = user.getRotationClient().x*-1;
        double y = Math.sin(Math.toRadians(rotation))*storedDistance;

        double hypot = Math.sqrt(Math.pow(storedDistance, 2) - Math.pow(y, 2));
        double rotation2 = user.getRotationClient().y*-1;
        double x = Math.sin(Math.toRadians(rotation2))*hypot;
        double z = Math.cos(Math.toRadians(rotation2))*hypot;
        return new Vec3d(x + user.getClientCameraPosVec(MC.getRenderTickCounter().getTickProgress(false)).x, y + user.getClientCameraPosVec(MC.getRenderTickCounter().getTickProgress(false)).y, z + user.getClientCameraPosVec(MC.getRenderTickCounter().getTickProgress(false)).z);
    }

    public static NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for(Cutscene cutscene : cutscenes)
            list.add(cutscene.toNbt());
        nbt.put("CutsceneList", list);
        return nbt;
    }

    @Override
    public void onInitializeClient() {
		WorldRenderEvents.AFTER_ENTITIES.register(this::renderBeziers);
        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("Request", true);
            ClientPlayNetworking.send(new CutscenePayload(nbt));
        });
    }
}
