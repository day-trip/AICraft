package com.daytrip.aicraft.navigation;

import com.daytrip.aicraft.pathfinding.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class PlayerLookController implements Control {
    protected final LocalPlayer player;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public PlayerLookController(LocalPlayer player) {
        this.player = player;
    }

    public void setLookAt(Vec3 vec3) {
        this.setLookAt(vec3.x, vec3.y, vec3.z);
    }

    public void setLookAt(Entity entity) {
        this.setLookAt(entity.getX() + entity.getBbWidth(), entity.getY() + (entity.getBbHeight() / 2f), entity.getZ() + entity.getBbWidth());
    }

    public void setLookAt(Entity entity, float f, float g) {
        this.setLookAt(entity.getX(), PlayerLookController.getWantedY(entity), entity.getZ(), f, g);
    }

    public void setLookAt(double d, double e, double f) {
        // this.setLookAt(d, e, f, 25, 90);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(d, e, f));
    }

    public void setLookAt(double d, double e, double f, float g, float h) {
        this.wantedX = d;
        this.wantedY = e;
        this.wantedZ = f;
        this.yMaxRotSpeed = g;
        this.xMaxRotAngle = h;
        this.lookAtCooldown = 80;
    }

    public void tick() {
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent(float_ -> this.player.yHeadRot = this.rotateTowards(this.player.yHeadRot, float_, this.yMaxRotSpeed));
            this.getXRotD().ifPresent(float_ -> this.player.setXRot(this.rotateTowards(this.player.getXRot(), float_, this.xMaxRotAngle)));
            System.out.println(new Pair<>(this.getYRotD(), this.getXRotD()));
        } else {
            this.player.yHeadRot = this.rotateTowards(this.player.yHeadRot, this.player.yBodyRot, 10.0f);
        }
        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody() {
        this.player.yHeadRot = Mth.rotateIfNecessary(this.player.yHeadRot, this.player.yBodyRot, 180);
    }

    public boolean lookingAtTarget(double threshold) {
        if (getYRotD().isEmpty() || getXRotD().isEmpty()) {
            return false;
        }

        return Mth.degreesDifference(player.yHeadRot, getYRotD().get()) < threshold/* && Mth.degreesDifference(player.getXRot(), getXRotD().get()) < threshold*/;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    public Vec3 wanted() {
        return new Vec3(wantedX, wantedY, wantedZ);
    }

    protected Optional<Float> getXRotD() {
        double d = this.wantedX - this.player.getX();
        double e = this.wantedY - this.player.getEyeY();
        double f = this.wantedZ - this.player.getZ();
        double g = Math.sqrt(d * d + f * f);
        return Math.abs(e) > (double)1.0E-5f || Math.abs(g) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(-(Mth.atan2(e, g) * 57.2957763671875)))) : Optional.empty();
    }

    protected Optional<Float> getYRotD() {
        double d = this.wantedX - this.player.getX();
        double e = this.wantedZ - this.player.getZ();
        return Math.abs(e) > (double)1.0E-5f || Math.abs(d) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f)) : Optional.empty();
    }

    protected float rotateTowards(float f, float g, float h) {
        float i = Mth.degreesDifference(f, g);
        float j = Mth.clamp(i, -h, h);
        return f + j;
    }

    private static double getWantedY(Entity entity) {
        /*if (entity instanceof LivingEntity) {
            return entity.getEyeY();
        }*/
        return (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
    }
}
