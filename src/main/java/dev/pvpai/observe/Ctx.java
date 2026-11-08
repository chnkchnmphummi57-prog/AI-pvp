package dev.pvpai.observe;

import dev.pvpai.config.ModConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** One-tick snapshot of the fight. Rules read fields directly; ML reads toFloats(). */
public final class Ctx {
    public float yaw, pitch, yawToTarget, pitchToTarget;
    public double dist, dx, dy, dz, velX, velY, velZ, tVelX, tVelY, tVelZ;
    public float health, maxHealth, tHealth, tMaxHealth, cooldown;
    public int hurtTime, tHurtTime;
    public boolean onGround, sprinting, tOnGround;
    public double fallDistance;
    public float tYaw, tPitch;

    public static Ctx build(ClientPlayerEntity p, LivingEntity t) {
        Ctx c = new Ctx();
        Vec3d eye = p.getEyePos();
        Vec3d aim = t.getBoundingBox().getCenter();
        c.dx = aim.x - eye.x; c.dy = aim.y - eye.y; c.dz = aim.z - eye.z;
        double h = Math.sqrt(c.dx * c.dx + c.dz * c.dz);
        c.dist = p.distanceTo(t);
        c.yaw = p.getYaw(); c.pitch = p.getPitch();
        c.yawToTarget = (float) (MathHelper.atan2(c.dz, c.dx) * 180.0 / Math.PI) - 90.0f;
        c.pitchToTarget = (float) (-(MathHelper.atan2(c.dy, h) * 180.0 / Math.PI));
        Vec3d v = p.getVelocity(), tv = t.getVelocity();
        c.velX = v.x; c.velY = v.y; c.velZ = v.z;
        c.tVelX = tv.x; c.tVelY = tv.y; c.tVelZ = tv.z;
        c.health = p.getHealth(); c.maxHealth = p.getMaxHealth();
        c.tHealth = t.getHealth(); c.tMaxHealth = t.getMaxHealth();
        c.cooldown = p.getAttackCooldownProgress(0.5f);
        c.hurtTime = p.hurtTime; c.tHurtTime = t.hurtTime;
        c.onGround = p.isOnGround(); c.tOnGround = t.isOnGround();
        c.sprinting = p.isSprinting();
        c.fallDistance = p.fallDistance;
        c.tYaw = MathHelper.wrapDegrees(t.getYaw() - c.yawToTarget);   // opponent facing relative to the line between us
        c.tPitch = t.getPitch();
        return c;
    }

    /** ML observation vector (single frame). History stacking happens in the bridge. */
    public float[] toFloats(ModConfig cfg, int heldItemId) {
        float[] o = new float[40];
        int i = 0;
        o[i++] = (float) c(dx / 6); o[i++] = (float) c(dy / 6); o[i++] = (float) c(dz / 6); o[i++] = (float) c(dist / 6);
        o[i++] = (float) c(velX); o[i++] = (float) c(velY); o[i++] = (float) c(velZ);
        o[i++] = (float) c(tVelX); o[i++] = (float) c(tVelY); o[i++] = (float) c(tVelZ);
        o[i++] = health / maxHealth; o[i++] = tHealth / tMaxHealth;
        o[i++] = cooldown;
        o[i++] = hurtTime / 10f; o[i++] = tHurtTime / 10f;
        o[i++] = MathHelper.wrapDegrees(yawToTarget - yaw) / 180f;
        o[i++] = (pitchToTarget - pitch) / 90f;
        o[i++] = yaw / 180f; o[i++] = pitch / 90f;
        o[i++] = tYaw / 180f; o[i++] = tPitch / 90f;
        o[i++] = onGround ? 1 : 0; o[i++] = tOnGround ? 1 : 0; o[i++] = sprinting ? 1 : 0;
        o[i++] = (float) c(fallDistance / 4);
        o[i++] = heldItemId; // replaced by one-hot in the bridge once item classes are fixed
        // toggle state, so one policy covers every toggle combination
        o[i++] = cfg.jump ? 1 : 0; o[i++] = cfg.crit ? 1 : 0; o[i++] = cfg.jumpReset ? 1 : 0;
        o[i++] = cfg.strafe ? 1 : 0; o[i++] = cfg.retreatLow ? 1 : 0; o[i++] = cfg.wTap ? 1 : 0;
        return o; // remaining slots reserved
    }

    private static double c(double v) { return Math.max(-1, Math.min(1, v)); }
}
