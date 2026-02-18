package zuyuku.yukuscutscenes.client.render;

import static zuyuku.yukuscutscenes.client.Client.MC;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import zuyuku.yukuscutscenes.YukusCutscenes;
import zuyuku.yukuscutscenes.client.util.ClientCutsceneManager;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.CutsceneManager;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.LerpType;
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

        ClientPlayNetworking.registerGlobalReceiver(CutscenePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String name = payload.nbt().getString("PlayName", "NULLNULL");
                if(!name.matches("NULLNULL")) {
                    for(Cutscene cutscene : cutscenes)
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
                cutscenes = CutsceneManager.makeList(payload.nbt());
            });
        });
    }
}
