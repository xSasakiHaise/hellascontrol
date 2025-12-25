package com.xsasakihaise.hellascontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal debug configuration for handshake diagnostics.
 */
public final class HellasControlDebugConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(HellasControlDebugConfig.class);

    private boolean debugHandshake = false;
    private boolean debugPacketRegistry = false;

    public boolean isDebugHandshake() {
        return debugHandshake;
    }

    public boolean isDebugPacketRegistry() {
        return debugPacketRegistry;
    }

    public static HellasControlDebugConfig load(Path configDir) {
        if (configDir == null) {
            LOGGER.info("[HellasControl] Debug config load skipped (null configDir)");
            return new HellasControlDebugConfig();
        }
        Path configPath = configDir.resolve("debug.json");
        if (!Files.exists(configPath)) {
            LOGGER.info("[HellasControl] Debug config not found at {}", configPath);
            return new HellasControlDebugConfig();
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            HellasControlDebugConfig loaded = GSON.fromJson(reader, HellasControlDebugConfig.class);
            LOGGER.info("[HellasControl] Debug config loaded from {}", configPath);
            return loaded == null ? new HellasControlDebugConfig() : loaded;
        } catch (IOException ex) {
            LOGGER.info("[HellasControl] Debug config failed to read from {}", configPath);
            return new HellasControlDebugConfig();
        }
    }
}
