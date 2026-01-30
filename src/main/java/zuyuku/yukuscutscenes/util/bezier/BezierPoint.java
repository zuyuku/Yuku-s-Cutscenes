package zuyuku.yukuscutscenes.util.bezier;

import static zuyuku.yukuscutscenes.client.Client.MC;

import java.util.ArrayList;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.DrawStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

public class BezierPoint {
    private static double size = 0.05;
    private BezierPath path;
    private Vec3d pos;
    private boolean isTangent;

    public BezierPoint(BezierSpline spline, Vec3d pos, boolean isTangent) {
        this.pos = pos;
        this.path = spline.path;
        this.isTangent = isTangent;
    }

    public boolean isSinglePoint() {
        return this.path.isSinglePoint();
    }

    public void setPos(Vec3d newPos) {
        if(!this.isSinglePoint()) {
            if(this.isTangent()) {
                if(!this.path.isPointFirstOrLast(this.getRoot()))
                    this.getMirrorTangent().pos = this.getRoot().getPos().subtract(newPos.subtract(this.getRoot().getPos()));
            } else {
                ArrayList<BezierPoint> tangents = this.getTangents();
                Vec3d offset = tangents.get(0).getPos().subtract(this.getPos());
                tangents.get(0).pos = newPos.add(offset);
                if(!this.isEnd())
                    tangents.get(1).pos = newPos.subtract(offset);
            }
        }
        this.pos = newPos;
        for(BezierSpline spline : this.getSplines())
            spline.updateCoeffs();
        this.path.updateLUT();
    }

    public ArrayList<BezierPoint> getTangents() {
        ArrayList<BezierPoint> send = new ArrayList<>();
        ArrayList<BezierPoint> points = this.path.getPoints();
        if(this.isTangent)
            return send;
        if(isLast())
            send.add(points.get(points.size()-2));
        else
            send.add(points.get(this.getIndex()+1));
        if(!isFirst())
            send.add(points.get(this.getIndex()-1));
        return send;
    }

    private int getIndex() {
        return this.path.getPoints().indexOf(this);
    }

    public ArrayList<BezierSpline> getSplines() {
        if(this.isTangent)
            return this.getRoot().getSplines();
        ArrayList<BezierSpline> send = new ArrayList<>();
        int index = this.getIndex();
        int splineIndex = (index-1)/3;
        send.add(this.path.splines.get(splineIndex));
        if(!this.isEnd())
            send.add(this.path.splines.get(splineIndex+1));
        return send;
    }

    public Vec3d getPos() {
        return this.pos;
    }

    public BezierPath getPath() {
        return this.path;
    }

    public void render(WorldRenderContext context) {
        if(this.path == null)
            return;
        if(MC.player.getEyePos().distanceTo(this.pos) < 0.1)
            return;
        int color = this.getColor();
        if(this.isTangent())
            GizmoDrawing.line(this.pos, this.getRoot().pos, ColorHelper.scaleAlpha(this.getColor(), 0.5f), 5);
        if(this.isHovered(MC.player))
            color = ColorHelper.fromFloats(1.0f, 1.0f, 1.0f, 1.0f);
        if(this.path.isSinglePoint())
            color = ColorHelper.fromFloats(1.0f, 0.0f, 1.0f, 0.0f);
        GizmoDrawing.box(new Box(this.pos.add(new Vec3d(size,size,size)), this.pos.subtract(new Vec3d(size,size,size))), DrawStyle.stroked(color), true);
    }

    private int getColor() {
        int red = ColorHelper.fromFloats(1.0F, 1.0F, 0.0F, 0.0F);
        int blue = ColorHelper.fromFloats(1.0F, 0.0F, 0.0F, 1.0F);
        if(this.isTangent) {
            if(this.getRoot().isFirst())
                return red;
            if(this.getRoot().isLast())
                return blue;
        } else {
            if(this.isFirst())
                return red;
            if(this.isLast())
                return blue;
        }
        return ColorHelper.fromFloats(1.0f, 0.5f, 0.5f, 0.5f);
    }

    public boolean isEnd() {
        return this.isTangent ? false : this.isFirst() || this.isLast();
    }

    public boolean isFirst() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getFirst() == this;
    }

    public boolean isLast() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getLast() == this;
    }

    public boolean isHovered(LivingEntity entity) {
        double x = this.pos.x - entity.getEyePos().x;
        double y = this.pos.y - entity.getEyePos().y;
        double z = this.pos.z - entity.getEyePos().z;
        double rotation = Math.toDegrees(Math.atan2(x,z))*-1;
        double playerRot = MathHelper.wrapDegrees(entity.getRotationClient().y);

        double playerRot2 = MathHelper.wrapDegrees(entity.getRotationClient().x);
        double hypot = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
        double rotation2 = Math.toDegrees(Math.atan2(y,hypot))*-1;
        
        if(Math.abs(playerRot - rotation) < 1 && Math.abs(playerRot2 - rotation2) <1)
            return true;
        return false;
    }

    public BezierPoint getRoot() {
        if(!this.isTangent())
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (2*(index % 3)-3);
        return points.get(alter);
    }

    public BezierPoint getMirrorTangent() {
        if(!this.isTangent() || this.path.isPointFirstOrLast(this.getRoot()))
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (-2*((index+1)%3)+2);
        return points.get(alter);
    }

    public boolean isTangent() {
        return this.isTangent;
    }

    public boolean canBeModified() {
        return !this.isTangent;
    }
}
