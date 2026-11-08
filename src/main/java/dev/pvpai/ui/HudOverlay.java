package dev.pvpai.ui;

import dev.pvpai.act.Action;
import dev.pvpai.config.ModConfig;
import dev.pvpai.core.AiController;
import dev.pvpai.core.ScopeGate;
import dev.pvpai.observe.Ctx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Live panel. Step 3 adds training-progress graphs, tier badge and Elo here. */
public final class HudOverlay {
    private HudOverlay() { }

    public static void render(DrawContext g, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.player == null) return;
        ModConfig cfg = ModConfig.get();
        AiController ai = AiController.INSTANCE;
        Ctx c = ai.lastCtx; Action a = ai.lastAction;

        int x = 6, y = 6, w = 150, lh = 11;
        int lines = 11;
        g.fill(x - 3, y - 3, x + w, y + lines * lh + 3, 0xB0101418);
        g.fill(x - 3, y - 3, x - 1, y + lines * lh + 3, cfg.enabled ? 0xFF3DDC84 : 0xFF6B7280);

        int ty = y;
        line(g, mc, "PvP AI  " + (cfg.enabled ? "ON" : "OFF"), x, ty, cfg.enabled ? 0xFF3DDC84 : 0xFFAAAAAA); ty += lh;
        line(g, mc, "Mode: " + cfg.mode.label, x, ty, 0xFFFFFFFF); ty += lh;
        line(g, mc, "State: " + ai.status, x, ty, 0xFFFFFFFF); ty += lh;
        line(g, mc, "Scope: " + ScopeGate.describe(mc), x, ty, 0xFFB0B0B0); ty += lh;
        line(g, mc, c == null ? "Dist: -" : String.format("Dist: %.2f  CD: %.2f", c.dist, c.cooldown), x, ty, 0xFFFFFFFF); ty += lh;
        line(g, mc, c == null ? "HP: -" : String.format("HP: %.0f / opp %.0f", c.health, c.tHealth), x, ty, 0xFFFFFFFF); ty += lh;
        line(g, mc, a == null ? "Keys: -" : "Keys: " + (a.forward ? "W" : "") + (a.left ? "A" : "") + (a.back ? "S" : "")
                + (a.right ? "D" : "") + (a.jump ? " JUMP" : "") + (a.attack ? " HIT" : ""), x, ty, 0xFFFFFFFF); ty += lh;
        line(g, mc, "Toggles: " + t("J", cfg.jump) + t("C", cfg.crit) + t("R", cfg.jumpReset) + t("S", cfg.strafe)
                + t("L", cfg.retreatLow) + t("W", cfg.wTap), x, ty, 0xFFB0B0B0); ty += lh;
        line(g, mc, "Model: none (step 4)", x, ty, 0xFF8A8A8A); ty += lh;
        line(g, mc, "Tier: n/a (step 3)", x, ty, 0xFF8A8A8A); ty += lh;
        line(g, mc, "Elo: n/a (step 3)", x, ty, 0xFF8A8A8A);
    }

    private static String t(String k, boolean on) { return on ? k + " " : "- "; }

    private static void line(DrawContext g, MinecraftClient mc, String s, int x, int y, int color) {
        g.drawText(mc.textRenderer, s, x, y, color, false);
    }
}
