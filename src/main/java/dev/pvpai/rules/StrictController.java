package dev.pvpai.rules;

import dev.pvpai.act.Action;
import dev.pvpai.config.ModConfig;
import dev.pvpai.observe.Ctx;
import net.minecraft.util.math.MathHelper;

/** Deterministic sword-PvP controller. Same input -> same output. Smart Mode wraps this later. */
public final class StrictController {
    public enum State { APPROACH, ENGAGE, RETREAT }

    public State state = State.APPROACH;
    private int strafeTick = 0, wTapTicks = 0;
    private boolean strafeRight = true;

    public Action decide(Ctx c, ModConfig cfg) {
        Action a = new Action();
        float yawErr = MathHelper.wrapDegrees(c.yawToTarget - c.yaw);
        float pitchErr = c.pitchToTarget - c.pitch;
        int max = cfg.maxTurnDegPerTick;
        a.dYaw = MathHelper.clamp(yawErr, -max, max);
        a.dPitch = MathHelper.clamp(pitchErr, -max / 2f, max / 2f);

        boolean critOn = cfg.crit && cfg.jump;
        boolean retreating = cfg.retreatLow && c.health <= cfg.retreatHealth;
        state = retreating ? State.RETREAT : (c.dist > cfg.engageDist ? State.APPROACH : State.ENGAGE);

        // Movement
        switch (state) {
            case RETREAT -> { a.back = true; a.sprint = false; }
            case APPROACH -> { a.forward = true; a.sprint = true; }
            case ENGAGE -> {
                a.sprint = wTapTicks <= 0;
                if (c.dist < cfg.minDist) a.back = true; else a.forward = true;
            }
        }
        if (wTapTicks > 0) { wTapTicks--; a.forward = false; a.sprint = false; }

        // Strafing (fixed period in Strict)
        if (cfg.strafe && c.dist < cfg.engageDist + 1.5) {
            if (++strafeTick >= cfg.strafePeriodTicks) { strafeTick = 0; strafeRight = !strafeRight; }
            a.right = strafeRight; a.left = !strafeRight;
        }

        // Jump: crit setup and jump reset
        if (cfg.jump) {
            if (critOn && c.onGround && c.dist < cfg.reach + 0.8 && c.cooldown >= 0.7f) a.jump = true;
            if (cfg.jumpReset && c.hurtTime >= 8 && c.onGround) a.jump = true;
        }

        // Attack
        boolean inReach = c.dist <= cfg.reach;
        boolean aimed = Math.abs(yawErr) < 12 && Math.abs(pitchErr) < 15;
        boolean ready = c.cooldown >= 0.92f;
        if (inReach && aimed && ready) {
            a.attack = critOn ? (!c.onGround && c.fallDistance > 0 && c.velY < 0) : true;
            if (a.attack && cfg.wTap) wTapTicks = 1;
        }
        return a;
    }
}
