package dev.pvpai;

import dev.pvpai.config.ModConfig;
import dev.pvpai.core.AiController;
import dev.pvpai.core.ScopeGate;
import dev.pvpai.ui.ConfigScreenFactory;
import dev.pvpai.ui.HudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class PvpAiClient implements ClientModInitializer {
    public static final String ID = "pvpai";

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        // 1.21.9+ keybinds need a Category object (if this line fails to compile, check KeyBinding.Category in your Yarn build)
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of(ID, "main"));
        KeyBinding toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.pvpai.toggle", GLFW.GLFW_KEY_RIGHT_BRACKET, cat));
        KeyBinding menu = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.pvpai.menu", GLFW.GLFW_KEY_LEFT_BRACKET, cat));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (toggle.wasPressed()) {
                ModConfig cfg = ModConfig.get();
                if (!cfg.enabled && !ScopeGate.allowed(mc)) {
                    if (mc.player != null) mc.player.sendMessage(Text.literal("[PvP AI] Blocked: only singleplayer or allowlisted servers."), true);
                } else {
                    cfg.enabled = !cfg.enabled;
                }
            }
            while (menu.wasPressed()) mc.setScreen(ConfigScreenFactory.create(mc.currentScreen));
            if (mc.player != null && mc.world != null) AiController.INSTANCE.tick(mc);
        });

        HudRenderCallback.EVENT.register(HudOverlay::render);
    }
}
