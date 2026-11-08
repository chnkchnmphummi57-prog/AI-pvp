package dev.pvpai.act;

import dev.pvpai.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public final class ActionApplier {
    private ActionApplier() { }

    /** Optional hard mask. For rule modes this is a safety net; ML/Smart also get reward penalties later. */
    public static void mask(Action a, ModConfig cfg) {
        if (!cfg.strictEnforcement) return;
        if (!cfg.jump) a.jump = false;
        if (!cfg.strafe) { a.left = false; a.right = false; }
        // Retreat When Low disabled -> never back off because of health; spacing back-steps are unaffected.
    }

    public static void apply(MinecraftClient mc, Action a, Entity target) {
        ClientPlayerEntity p = mc.player;
        var o = mc.options;
        o.forwardKey.setPressed(a.forward);
        o.backKey.setPressed(a.back);
        o.leftKey.setPressed(a.left);
        o.rightKey.setPressed(a.right);
        o.jumpKey.setPressed(a.jump);
        o.sprintKey.setPressed(a.sprint);
        p.setYaw(p.getYaw() + a.dYaw);
        p.setPitch(MathHelper.clamp(p.getPitch() + a.dPitch, -90f, 90f));
        if (a.attack && mc.interactionManager != null) {
            mc.interactionManager.attackEntity(p, target);
            p.swingHand(Hand.MAIN_HAND);
        }
    }
}
