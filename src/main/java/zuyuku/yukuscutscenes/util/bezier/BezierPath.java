package zuyuku.yukuscutscenes.util.bezier;

import java.util.ArrayList;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;
import zuyuku.yukuscutscenes.client.render.CurveRenderer;
import zuyuku.yukuscutscenes.util.Cutscene;

public class BezierPath {
    private static final int n = 100;
    
    public ArrayList<BezierSpline> splines = new ArrayList<>();
    private double[] LUT = new double[n];
    private Cutscene cutscene;

    public BezierPath(Vec3d startPos) {
        this.splines.add(new BezierSpline(startPos, this));
        this.updateLUT();
    }

    public boolean isSinglePoint() {
        return this.splines.size()==1 && this.splines.getFirst().isSinglePoint();
    }

    public BezierPath withPlayerOrigin(PlayerEntity player) {
        BezierPath newPath = new BezierPath(Vec3d.ZERO);
        ArrayList<BezierSpline> tempSplines = new ArrayList<>();
        for(BezierSpline spline : this.splines)
            tempSplines.add(spline);
        if(tempSplines.getFirst().isSinglePoint()) {
            tempSplines.remove(0);
            tempSplines.addFirst(new BezierSpline(this.getPoints().getFirst(), this, player, true));
        }
        else
            tempSplines.addFirst(new BezierSpline(this.getPoints().getFirst(), this, player));
        newPath.splines = tempSplines;
        newPath.updateLUT();
        return newPath;
    }

    public BezierPath withPlayerEnd(PlayerEntity player) {
        BezierPath newPath = new BezierPath(Vec3d.ZERO);
        ArrayList<BezierSpline> tempSplines = new ArrayList<>();
        for(BezierSpline spline : this.splines)
            tempSplines.add(spline);
        if(tempSplines.getFirst().isSinglePoint()) {
            tempSplines.remove(0);
            tempSplines.add(new BezierSpline(this.getPoints().getLast(), this, player, false));
        }
        else
            tempSplines.add(new BezierSpline(this.getPoints().getLast(), this, player));
        newPath.splines = tempSplines;
        newPath.updateLUT();
        return newPath;
    }

    public void render(WorldRenderContext context) {
        for(BezierSpline spline : splines)
            spline.render(context);
        for(BezierPoint point : getPoints())
            point.render(context);
    }

    private double distanceToT(double t) {
        if(t < 0.0 || t > 1.0)
            return t;
        double maxDistance = LUT[n-1];
        double desiredDistance = maxDistance*t;
        int index = findClosestFloorIndex(desiredDistance);
        double between = (desiredDistance-LUT[index])/(LUT[index+1]-LUT[index]);
        return ((double)index + between)/(double)(n-1);
    }

    private int findClosestFloorIndex(double distance) {
        for(int i = 1; i<n; i++)
            if (LUT[i]>=distance)
                return i-1;
        return n-2;
    }

    public Cutscene getClientCutscene() {
        if(this.cutscene == null) {
            for(Cutscene cutscene : CurveRenderer.cutscenes)
                if(cutscene.path == this)
                    return cutscene;
                throw new IllegalArgumentException("No cutscene was found for this path.");
        }
        return cutscene;
    }

    public void updateLUT() {
        LUT[0] = 0.0;
        Vec3d prevPoint = this.splines.get(0).lerp(0);
        double totalDist = 0;
        for(int i = 1; i<n; i++) {
            Vec3d nextPoint = lerp(i/(double)(n-1));
            double distance = prevPoint.distanceTo(nextPoint);
            totalDist += distance;
            LUT[i] = totalDist;
            prevPoint = nextPoint;
        }
    }

    private Vec3d lerp(double t) {
        if(t>=1.0)
            return this.splines.getLast().lerp(1.0);
        t = t*splines.size();
        return this.splines.get((int)t).lerp(t%1);
    }

    public Vec3d lerpSpeedWeighted(double t) {
        return lerp(distanceToT(t));
    }

    public ArrayList<BezierPoint> getPoints() {
        ArrayList<BezierPoint> send = new ArrayList<>();
        for(BezierSpline spline : this.splines)
            for(BezierPoint point : spline.points)
                if(!send.contains(point))
                    send.add(point);
        return send;
    }

    public void removePoint(BezierPoint point) {
        if(this.splines.size() == 1) {
            BezierSpline spline = this.splines.getFirst();
            if(spline.isSinglePoint()) 
                for(Cutscene cutscene : CurveRenderer.cutscenes)
                    if(cutscene.path == this) {
                        CurveRenderer.cutscenes.remove(cutscene);
                        return;
                    }
            if(point.isLast()) {
                this.splines.set(0, new BezierSpline(spline.v1.getPos(), this));
                for(Cutscene cutscene : CurveRenderer.cutscenes)
                        if(cutscene.path == this)
                            cutscene.setFinalRot(cutscene.getRotAt(0));
            } else {
                this.splines.set(0, new BezierSpline(spline.v2.getPos(), this));
                for(Cutscene cutscene : CurveRenderer.cutscenes)
                        if(cutscene.path == this)
                            cutscene.setInitRot(cutscene.getRotAt(1));
            }
            return;
        }
        for(BezierSpline spline : point.getSplines())
            this.splines.remove(spline);
        this.updateLUT();
    }

    public boolean isPointFirstOrLast(BezierPoint point) {
        ArrayList<BezierPoint> points = this.getPoints();
        if(points.getFirst() == point)
            return true;
        if(points.getLast() == point)
            return true;
        return false;
    }

    public BezierPath(NbtList list) {
        this.init(list);
    }

    private void init(NbtList list) {
        ArrayList<Vec3d> points = new ArrayList<>();
        int pointCount = list.size()/3;
        
        for(int i = 0; i<pointCount; i++)
            points.add(new Vec3d(list.getDouble((i*3), 0), list.getDouble((i*3)+1, 0), list.getDouble((i*3)+2, 0)));
        ArrayList<BezierSpline> splines = new ArrayList<>();
        int splineCount = (points.size()-1)/3;
        for(int i = 0; i<splineCount; i++) {
            if(i==0)
                splines.add(new BezierSpline(points.get(0), points.get(1), points.get(2), points.get(3), this));
            else
                splines.add(new BezierSpline(splines.get(i-1), points.get(1), points.get(2), points.get(3), this));
            points.removeFirst();
            points.removeFirst();
            points.removeFirst();
        }
        if(splineCount==0)
            splines.add(new BezierSpline(points.getFirst(), this));
        this.splines = splines;
        this.updateLUT();
    }

    public NbtList toNbt() {
        NbtList list = new NbtList();
        for(BezierPoint point : this.getPoints()) {
            list.add(NbtDouble.of(point.getPos().x));
            list.add(NbtDouble.of(point.getPos().y));
            list.add(NbtDouble.of(point.getPos().z));
        }
        return list;
    }
}
