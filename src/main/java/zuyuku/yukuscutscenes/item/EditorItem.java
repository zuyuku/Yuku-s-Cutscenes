package zuyuku.yukuscutscenes.item;

import static zuyuku.yukuscutscenes.client.Client.MC;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import zuyuku.yukuscutscenes.client.render.CurveRenderer;
import zuyuku.yukuscutscenes.util.Cutscene;
import zuyuku.yukuscutscenes.util.CutscenePayload;
import zuyuku.yukuscutscenes.util.bezier.BezierPath;
import zuyuku.yukuscutscenes.util.bezier.BezierPoint;
import zuyuku.yukuscutscenes.util.bezier.BezierSpline;

public class EditorItem extends Item {

    private double storedDistance;
    private BezierPoint storedPoint;

    public EditorItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        player.setCurrentHand(hand);
        return ActionResult.PASS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
	public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if(!world.isClient())
            return true;
        if(getMaxUseTime(stack, user) - remainingUseTicks <= 2 && this.getStoredPoint(user) != null) {
            if(user.isSneaking() && storedPoint.isEnd())
                this.storedPoint.getPath().removePoint(storedPoint);
            else if(!this.storedPoint.isEnd())
                MC.player.sendMessage(Text.literal("To add or delete a spline, please select an endpoint.").formatted(Formatting.RED), true);
            else {
                BezierPath path = this.storedPoint.getPath();
                BezierSpline newSpline = new BezierSpline(storedPoint, path, user.getEyePos());
                if(this.storedPoint.isFirst())
                    path.splines.addFirst(newSpline);
                else 
                    path.splines.addLast(newSpline);
                path.updateLUT();
            }
        }
        this.storedPoint = null;
        CurveRenderer.storedPoint = null;
        ClientPlayNetworking.send(new CutscenePayload(CurveRenderer.toNbt()));
        return true;
	}
    
    @Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if(!world.isClient() || getMaxUseTime(stack, user) - remainingUseTicks <= 2 || this.getStoredPoint(user) == null)
            return;
        if(CurveRenderer.storedPoint == null) {
            CurveRenderer.storedPoint = this.storedPoint;
            if(this.storedPoint.isEnd())
                CurveRenderer.storedDistance = 0.0;
            else
                CurveRenderer.storedDistance = this.storedDistance;
        }
    }

    private BezierPoint getStoredPoint(LivingEntity user) {
        if(this.storedPoint != null)
            return this.storedPoint;
        for(Cutscene cutscene : CurveRenderer.cutscenes)
            for(BezierSpline spline : cutscene.path.splines)
                for(BezierPoint point : spline.points)
                    if(point.isHovered(user)) {
                        this.storedPoint = point;
                        this.storedDistance = point.getPos().distanceTo(user.getEyePos());
                    }
        return storedPoint;
    }
}
