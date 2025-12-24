package com.xsasakihaise.hellascontrol.bisect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class AutoBisectConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    boolean enabled = false;
    String launchCmd = "";
    String modsDir = "";
    String tempRoot = "mod_bisect_runs";
    int maxLogLines = 200;
    List<String> baseline = new ArrayList<>();
    List<String> copyDirs = List.of("config", "defaultconfigs");

    static AutoBisectConfig load(Path serverRoot) {
        Path configDir = serverRoot.resolve("config").resolve("hellas");
        Path configPath = configDir.resolve("bisect.json");
        if (!Files.exists(configPath)) {
            return new AutoBisectConfig();
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            AutoBisectConfig loaded = GSON.fromJson(reader, AutoBisectConfig.class);
            return loaded == null ? new AutoBisectConfig() : loaded;
        } catch (IOException ex) {
            return new AutoBisectConfig();
        }
    }
}
