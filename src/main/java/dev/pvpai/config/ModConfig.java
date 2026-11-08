package dev.pvpai.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.pvpai.core.AiMode;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pvpai.json");
    private static ModConfig instance;

    public AiMode mode = AiMode.STRICT;
    public boolean enabled = false;          // always starts OFF each launch

    // Movement toggles
    public boolean jump = true, crit = true, jumpReset = true, strafe = true, retreatLow = true, wTap = true;
    public boolean strictEnforcement = true; // mask disabled actions at the act layer (ML/Smart soft penalty comes later)

    // Tuning
    public double retreatHealth = 6.0;
    public int maxTurnDegPerTick = 18;
    public double engageDist = 2.9, minDist = 1.8, reach = 3.0, targetRange = 12.0;
    public int strafePeriodTicks = 14;

    // Scope gate: singleplayer is always allowed. Add ONLY servers you own / run.
    public List<String> allowedServers = new ArrayList<>();

    public static ModConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) instance = GSON.fromJson(Files.readString(FILE), ModConfig.class);
        } catch (Exception ignored) { }
        if (instance == null) instance = new ModConfig();
        instance.enabled = false;
    }

    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(get())); } catch (IOException ignored) { }
    }
}
