package zuyuku.yukuscutscenes.util.bezier;

import java.util.List;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.GizmoDrawing;

public class BezierSpline {
    public BezierPoint v1;
    public BezierPoint v1_tangent;
    public BezierPoint v2;
    public BezierPoint v2_tangent;
    public BezierPath path;

    public List<BezierPoint> points;

    private Vec3d coeff1;
    private Vec3d coeff2;
    private Vec3d coeff3;

    public void render(WorldRenderContext context) {
        double delta = 0.01;
        for(double d = 0.00; d<1; d+=delta) {
            Vec3d pos1 = lerp(d);
            Vec3d pos2 = lerp(d+delta);
            GizmoDrawing.line(pos1, pos2, ColorHelper.fromFloats(0.7F, 1.0F, 1.0F, 1.0F));
        }
    }

    public boolean isSinglePoint() {
        return this.points.size()==1;
    }

    public boolean containsPoint(BezierPoint point) {
        for(BezierPoint hasPoint : this.points)
            if(hasPoint == point)
                return true;
        return false;
    }

    public BezierSpline(Vec3d v1, Vec3d v1_tangent, Vec3d v2_tangent, Vec3d v2, BezierPath path) {
        this.path = path;
        buildInit(v1, v1_tangent, v2_tangent, v2);
    }

    private void buildInit(Vec3d v1, Vec3d v1_tangent, Vec3d v2_tangent, Vec3d v2) {
        this.v1 = new BezierPoint(this, v1, false);
        this.v1_tangent = new BezierPoint(this, v1_tangent, true);
        this.v2_tangent = new BezierPoint(this, v2_tangent, true);
        this.v2 = new BezierPoint(this, v2, false);
        points = List.of(this.v1,this.v1_tangent,this.v2_tangent,this.v2);
        updateCoeffs();
    }

    public BezierSpline(BezierSpline prevSpline, Vec3d v1_tangent, Vec3d v2_tangent, Vec3d v2, BezierPath path) {
        this.path = path;
        buildInit(prevSpline, v1_tangent, v2_tangent, v2);
    }

    private void buildInit(BezierSpline prevSpline, Vec3d v1_tangent, Vec3d v2_tangent, Vec3d v2) {
        this.v1 = prevSpline.v2;
        this.v1_tangent = new BezierPoint(this, v1_tangent, true);
        this.v2_tangent = new BezierPoint(this, v2_tangent, true);
        this.v2 = new BezierPoint(this, v2, false);
        points = List.of(this.v1,this.v1_tangent,this.v2_tangent,this.v2);
        updateCoeffs();
    }

    public BezierSpline(Vec3d initialPos, BezierPath owner) {
        this.path = owner;
        this.v1 = new BezierPoint(this, initialPos, false);
        points = List.of(v1);
        updateCoeffs();
    }

    public BezierSpline(BezierPoint point, BezierPath owner, Vec3d newEnd) {
        this.path = owner;
        if(point.isSinglePoint()) {
            this.v1 = point;
            this.v2 = new BezierPoint(this, newEnd, false);
            Vec3d offset = v1.getPos().subtract(v2.getPos()).multiply(0.5);
            this.v1_tangent= new BezierPoint(this, v1.getPos().subtract(offset), true);
            this.v2_tangent = new BezierPoint(this, v1.getPos().subtract(offset), true);

        } else if(point.isFirst()) {
            this.v2 = point;
            Vec3d offset = v2.getTangents().get(0).getPos().subtract(v2.getPos());
            this.v2_tangent = new BezierPoint(this, v2.getPos().subtract(offset), true);
            this.v1 = new BezierPoint(this, newEnd, false);
            this.v1_tangent= new BezierPoint(this, v1.getPos().subtract(offset), true);
        } else {
            this.v1 = point;
            Vec3d offset = v1.getTangents().get(0).getPos().subtract(v1.getPos());
            this.v1_tangent= new BezierPoint(this, v1.getPos().subtract(offset), true);
            this.v2 = new BezierPoint(this, newEnd, false);
            this.v2_tangent = new BezierPoint(this, v2.getPos().subtract(offset), true);
        }
        points = List.of(v1,v1_tangent,v2_tangent,v2);
        updateCoeffs();
        this.path.updateLUT();
    }

    public BezierSpline(BezierPoint point, BezierPath owner, PlayerEntity player, boolean startAtPlayer) {
        this.path = owner;
        if(startAtPlayer) {
            this.v2 = new BezierPoint(this, point.getPos(), false);
            this.v2_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePos()).multiply(0.5).add(player.getEyePos()), true);
            this.v1_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePos()).multiply(0.5).add(player.getEyePos()), true);
            this.v1 = new BezierPoint(this, player.getEyePos(), false);
        } else {
            this.v1 = new BezierPoint(this, point.getPos(), false);;
            this.v2_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePos()).multiply(0.5).add(player.getEyePos()), true);
            this.v1_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePos()).multiply(0.5).add(player.getEyePos()), true);
            this.v2 = new BezierPoint(this, player.getEyePos(), false);
        }
        points = List.of(v1,v1_tangent,v2_tangent,v2);
        updateCoeffs();
    }

    public BezierSpline(BezierPoint point, BezierPath owner, PlayerEntity player) {
        this.path = owner;
        if(point.isFirst()) {
            this.v2 = point;
            Vec3d offset = v2.getPos().subtract(v2.getTangents().get(0).getPos().subtract(v2.getPos()));
            this.v2_tangent = new BezierPoint(this, offset, true);
            this.v1_tangent= new BezierPoint(this, offset, true);
            this.v1 = new BezierPoint(this, player.getEyePos(), false);
        } else {
            this.v1 = point;
            Vec3d offset = v1.getPos().subtract(v1.getTangents().get(0).getPos().subtract(v1.getPos()));
            this.v1_tangent = new BezierPoint(this, offset, true);
            this.v2_tangent= new BezierPoint(this, offset, true);
            this.v2 = new BezierPoint(this, player.getEyePos(), false);
        }
        points = List.of(v1,v1_tangent,v2_tangent,v2);
        updateCoeffs();
    }

    public void updateCoeffs() {
        if(this.isSinglePoint())
            return;
        coeff1 = v1.getPos().multiply(-1).add(v1_tangent.getPos().multiply(3)).add(v2_tangent.getPos().multiply(-3)).add(v2.getPos());
        coeff2 = v1.getPos().multiply(3).add(v1_tangent.getPos().multiply(-6)).add(v2_tangent.getPos().multiply(3));
        coeff3 = v1.getPos().multiply(-3).add(v1_tangent.getPos().multiply(3));
    }

    public Vec3d lerp(double t) {
        return this.isSinglePoint() ? this.points.getFirst().getPos() : coeff1.multiply(Math.pow(t, 3)).add(
            coeff2.multiply(Math.pow(t, 2)).add(
                coeff3.multiply(t).add(
                    v1.getPos()
                )
            )
        );
    }
}
