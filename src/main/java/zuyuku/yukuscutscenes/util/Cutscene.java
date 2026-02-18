package zuyuku.yukuscutscenes.util;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.debug.gizmo.TextGizmo;
import zuyuku.yukuscutscenes.client.render.CurveRenderer;
import zuyuku.yukuscutscenes.util.bezier.BezierPath;

public class Cutscene {
    private static final double dist = 0.5;
    private static final double width = 0.9;
    private static final double height =  0.475;
    public static final List<Vec3d> relativePoints = List.of(new Vec3d(dist, height, width), new Vec3d(dist, height, -1*width), new Vec3d(dist, -1*height, -1*width), new Vec3d(dist, -1*height, width));
    
    public final BezierPath path;
    private final String name;
    private Vec2f initialRot;
    private Vec2f finalRot;

    public Cutscene(String name, Vec2f initialRot, Vec2f finalRot, BezierPath path) {
        this.name = name;
        this.initialRot = initialRot;
        this.finalRot = finalRot;
        this.path = path;
    }

    public Cutscene originAtPlayer(PlayerEntity player) {
        BezierPath newPath = this.path.withPlayerOrigin(player);
        return new Cutscene(name, player.getRotationClient(), finalRot, newPath);
    }

    public Cutscene endAtPlayer(PlayerEntity player) {
        BezierPath newPath = this.path.withPlayerEnd(player);
        return new Cutscene(name, initialRot, player.getRotationClient(), newPath);
    }

    public void render(WorldRenderContext context) {
        Vec3d root = this.path.getPoints().getFirst().getPos();
        Vec3d tail = this.path.getPoints().getLast().getPos();
        int color = ColorHelper.fromFloats(1.0f, 0.5f, 0.5f, 0.5f);
        renderPointsRelative(root, relativePoints, initialRot, color);
        renderPointsRelative(tail, relativePoints, finalRot, color);
        if(CurveRenderer.storedPoint != null && CurveRenderer.storedPoint.isEnd() && CurveRenderer.storedDistance == 0) {
            GizmoDrawing.text(this.name, rotatePointRelative(root, relativePoints.get(1).add(new Vec3d(0, -.03, .03)), this.initialRot), TextGizmo.Style.centered(ColorHelper.fromFloats(1.0f, 1.0f, 1.0f, 1.0f)).scaled(0.075f));
            return;
        }
        GizmoDrawing.text(this.name, this.path.lerpSpeedWeighted(0.0), TextGizmo.Style.centered(ColorHelper.fromFloats(1.0f, 1.0f, 1.0f, 1.0f)));
        this.path.render(context);
    }

    public static void renderPointsRelative(Vec3d origin, List<Vec3d> relativePoints, Vec2f rot, int color) {
        ArrayList<Vec3d> translatedPoints = new ArrayList<>();
        for(Vec3d point : relativePoints)
            translatedPoints.add(rotatePointRelative(origin, point, rot));
        for(Vec3d point : translatedPoints)
            GizmoDrawing.line(origin, point, color);
        for(int i = 0; i < translatedPoints.size()-1; i++)
            GizmoDrawing.line(translatedPoints.get(i), translatedPoints.get(i+1), color);
        GizmoDrawing.line(translatedPoints.get(translatedPoints.size()-1), translatedPoints.get(0), color);
    }

    private static Vec3d rotatePointRelative(Vec3d origin, Vec3d relativeCoordinate, Vec2f rot) {
        double yawRadians = Math.toRadians(rot.y) + Math.PI*0.5;
        double pitchRadians = Math.toRadians(rot.x);
        double x = relativeCoordinate.x * Math.cos(yawRadians) * Math.cos(pitchRadians) + origin.x;
        double y = relativeCoordinate.x * Math.sin(pitchRadians) * -1 + origin.y;
        double z = relativeCoordinate.x * Math.sin(yawRadians) * Math.cos(pitchRadians) + origin.z;

        x -= relativeCoordinate.z * Math.sin(yawRadians);
        z += relativeCoordinate.z * Math.cos(yawRadians);

        x += relativeCoordinate.y * Math.sin(pitchRadians) * Math.cos(yawRadians);
        y += relativeCoordinate.y * Math.cos(pitchRadians);
        z += relativeCoordinate.y * Math.sin(pitchRadians) * Math.sin(yawRadians);

        // Vec3d point2 = new Vec3d(-1 * relativeCoordinate.z * Math.sin(yawRadians) + x, y, relativeCoordinate.z * Math.cos(yawRadians) + z);
        Vec3d point2 = new Vec3d(x, y, z);
        return point2;
    }

    public void setInitRot(Vec2f rot) {
        this.initialRot = rot;
        if(path.isSinglePoint())
            this.finalRot = rot;
    }

    public void setFinalRot(Vec2f rot) {
        this.finalRot = rot;
        if(path.isSinglePoint())
            this.initialRot = rot;
    }

    public Vec3d getPosAt(float t) {
        return path.lerpSpeedWeighted(t);
    }

    public Vec2f getRotAt(float t) {
        return new Vec2f(MathHelper.lerpAngleDegrees(t, initialRot.x, finalRot.x), MathHelper.lerpAngleDegrees(t, initialRot.y, finalRot.y));
    }

    public String getName() {
        return this.name;
    }

    public NbtCompound toNbt() {
        NbtCompound send = new NbtCompound();

        send.putString("Name", this.name);
        send.put("InitialRotation", Vec2f.CODEC, this.initialRot);
        send.put("FinalRotation", Vec2f.CODEC, this.finalRot);
        send.put("BezierPath", this.path.toNbt());

        return send;
    }

    public static Cutscene fromNbt(NbtCompound nbt) {
        Cutscene cutscene;
        cutscene = new Cutscene(
        nbt.getString("Name", "error"),
        nbt.get("InitialRotation", Vec2f.CODEC).get(),
        nbt.get("FinalRotation", Vec2f.CODEC).get(),
        new BezierPath(nbt.getListOrEmpty("BezierPath")));
            
        return cutscene;
    }
}
