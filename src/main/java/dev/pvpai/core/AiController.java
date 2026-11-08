package dev.pvpai.core;

import dev.pvpai.act.Action;
import dev.pvpai.act.ActionApplier;
import dev.pvpai.config.ModConfig;
import dev.pvpai.observe.Ctx;
import dev.pvpai.rules.StrictController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class AiController {
    public static final AiController INSTANCE = new AiController();

    public final StrictController strict = new StrictController();
    public volatile Ctx lastCtx;
    public volatile Action lastAction;
    public volatile String status = "idle";
    private boolean holdingKeys = false;

    public void tick(MinecraftClient mc) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.enabled) { release(); status = "off"; lastCtx = null; return; }
        if (!ScopeGate.allowed(mc)) { release(); cfg.enabled = false; status = "blocked: out of scope"; return; }
        if (mc.currentScreen != null) { release(); status = "paused (screen open)"; return; }

        LivingEntity target = findTarget(mc, cfg.targetRange);
        if (target == null) { release(); status = "no target"; lastCtx = null; return; }

        Ctx ctx = Ctx.build(mc.player, target);
        Action a = switch (cfg.mode) {
            case STRICT -> strict.decide(ctx, cfg);
            // ML and Smart land in steps 4-5; until then they fall back to Strict so the toggle is safe to test.
            case ML, SMART -> strict.decide(ctx, cfg);
        };
        ActionApplier.mask(a, cfg);
        ActionApplier.apply(mc, a, target);
        holdingKeys = true;
        lastCtx = ctx; lastAction = a;
        status = cfg.mode == AiMode.STRICT ? "strict: " + strict.state : cfg.mode.label + " (fallback: strict)";
    }

    private LivingEntity findTarget(MinecraftClient mc, double range) {
        PlayerEntity best = null;
        double bd = range;
        for (PlayerEntity other : mc.world.getPlayers()) {
            if (other == mc.player || !other.isAlive() || other.isSpectator()) continue;
            double d = mc.player.distanceTo(other);
            if (d < bd) { bd = d; best = other; }
        }
        return best;
    }

    private void release() {
        if (holdingKeys) { KeyBinding.updatePressedStates(); holdingKeys = false; }
    }
}
