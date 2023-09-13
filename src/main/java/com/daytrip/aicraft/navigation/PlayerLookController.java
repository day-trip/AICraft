package com.daytrip.aicraft.navigation;

import com.daytrip.aicraft.util.TickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/*public class PlayerLookController extends AIController {
    private Quaternion startPos;
    private Quaternion targetPos;
    private int value = -1;

    public PlayerLookController(LocalPlayer player) {
        super(player);
    }

    public void init(Vec3 target) {
        this.startPos = this.createQuaternion(player.getYRot(), player.getXRot(), 0);
        this.targetPos = this.createQuaternion(this.getYRotD(target).get(), this.getXRotD(target).get(), 0);
        System.out.println(startPos);
        System.out.println(target);
        this.value = 0;
    }

    @Override
    void tick() {
        if (this.value < 0) {
            return;
        }
        this.value++;
        if (this.value > 20) {
            this.value = -1;
            return;
        }
        Quaternion val = new Slerp(this.startPos, this.targetPos).apply((double) this.value / 20);
        Pair<Double, Double> pair = this.createEuler(val);
        this.player.setYRot(pair.first().floatValue());
        this.player.setXRot(pair.second().floatValue());
    }

    @Override
    boolean isActive() {
        return this.value >= 0 && this.value <= 20;
    }

    private Optional<Float> getXRotD(Vec3 target) {
        double d = target.x - this.player.getX();
        double e = target.y - this.player.getEyeY();
        double f = target.z - this.player.getZ();
        double g = Math.sqrt(d * d + f * f);
        return !(Math.abs(e) > 9.999999747378752E-6) && !(Math.abs(g) > 9.999999747378752E-6) ? Optional.empty() : Optional.of((float)(-(Mth.atan2(e, g) * 57.2957763671875)));
    }

    private Optional<Float> getYRotD(Vec3 target) {
        double d = target.x - this.player.getX();
        double e = target.z - this.player.getZ();
        return !(Math.abs(e) > 9.999999747378752E-6) && !(Math.abs(d) > 9.999999747378752E-6) ? Optional.empty() : Optional.of((float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0F);
    }

    private Quaternion createQuaternion(float yaw, float pitch, float roll) {
        *//*float qx = Mth.sin(roll/2) * Mth.cos(pitch/2) * Mth.cos(yaw/2) - Mth.cos(roll/2) * Mth.sin(pitch/2) * Mth.sin(yaw/2);
        float qy = Mth.cos(roll/2) * Mth.sin(pitch/2) * Mth.cos(yaw/2) + Mth.sin(roll/2) * Mth.cos(pitch/2) * Mth.sin(yaw/2);
        float qz = Mth.cos(roll/2) * Mth.cos(pitch/2) * Mth.sin(yaw/2) - Mth.sin(roll/2) * Mth.sin(pitch/2) * Mth.cos(yaw/2);
        float qw = Mth.cos(roll/2) * Mth.cos(pitch/2) * Mth.cos(yaw/2) + Mth.sin(roll/2) * Mth.sin(pitch/2) * Mth.sin(yaw/2);
        return Quaternion.of(qw, qx, qy, qz);*//*
        return Quaternion.of(1, pitch, yaw, 0);
    }

    private Pair<Double, Double> createEuler(Quaternion q) {
        *//*double x = q.getX();
        double y = q.getY();
        double z = q.getZ();
        double w = q.getW();
        double t0 = +2.0 * (w * x + y * z);
        double t1 = +1.0 - 2.0 * (x * x + y * y);
        double roll = Mth.atan2(t0, t1);
        double t2 = +2.0 * (w * y - z * x);
        t2 = t2 > 1.0 ? 1 : t2;
        t2 = t2 < -1.0 ? -1 : t2;
        double pitch = Math.asin(t2);
        double t3 = +2.0 * (w * z + x * y);
        double t4 = +1.0 - 2.0 * (y * y + z * z);
        double yaw = Mth.atan2(t3, t4);
        return new Pair<>(yaw, pitch);
        // return List.of(yaw, pitch, roll);*//*
        return new Pair<>(q.getY(), q.getX());
    }
}*/

public class PlayerLookController extends AIController {
    private float y;
    private float x;
    private double angleDistance;
    private TickTimer cameraInterpolationTimer;
    private Vec3 target = null;

    public PlayerLookController(LocalPlayer player) {
        super(player);
    }

    public void init(Vec3 target) {
        if (target.equals(this.target)) {
            return;
        }
        this.cameraInterpolationTimer = TickTimer.createNoAction(40 - 1, false);
        y = yawToFaceEntity(player.position(), target, 0);
        x = pitchToFaceEntity(player.position(),target, 0);
        this.target = target;
    }

    @Override
    void tick() {
        if (target == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        y = yawToFaceEntity(player.position(), target, 0);
        x = pitchToFaceEntity(player.position(), target, 0);

        angleDistance = Math.atan2(Math.sin(y - player.getYRot()), Math.cos(y - player.getYRot()));

        cameraInterpolationTimer.update();

        float progress = (cameraInterpolationTimer.getCurrentTicks() + minecraft.getDeltaFrameTime()) / cameraInterpolationTimer.getTargetTicks();
        player.setYRot((float) Math.toDegrees(angleLinearInterpolate((float) Math.toRadians(player.getYRot()), (float) Math.toRadians(y), progress)));
        player.setXRot((float) Math.toDegrees(angleLinearInterpolate((float) Math.toRadians(player.getXRot()), (float) Math.toRadians(x), progress)));
    }

    @Override
    boolean isActive() {
        return this.target != null && this.angleDistance > 5;
    }

    private float yawToFaceEntity(Vec3 attacker, Vec3 target, float yOffset) {
        double dirx = attacker.x - target.x;
        double diry = (attacker.y - target.y) + yOffset;
        double dirz = attacker.z - target.z;
        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

        dirx /= len;
        dirz /= len;

        double yaw = Math.atan2(dirz, dirx);

        yaw = yaw * 180.0 / Math.PI;

        yaw += 90f;

        return (float) yaw;
    }

    private float pitchToFaceEntity(Vec3 attacker, Vec3 target, float yOffset) {
        double dirx = attacker.x - target.x;
        double diry = (attacker.y - target.y) + yOffset;
        double dirz = attacker.z - target.z;
        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
        diry /= len;
        double pitch = Math.asin(diry);
        pitch = pitch * 180.0 / Math.PI;
        return (float) pitch;
    }

    private final float PI = 3.1415927f;
    private final float PI2 = PI * 2;

    private float angleLinearInterpolate(float fromRadians, float toRadians, float progress) {
        float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
        return (fromRadians + delta * progress + PI2) % PI2;
    }
}
