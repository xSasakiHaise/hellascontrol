package com.xsasakihaise.hellascontrol.bisect;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AutoBisectRunner {
    private static final Logger LOGGER = LogManager.getLogger(AutoBisectRunner.class);
    private static final Marker DIAGNOSTICS = MarkerManager.getMarker("HELLASCONTROL_BISECT");
    private static final String CHILD_ENV = "HELLASCONTROL_BISECT_CHILD";
    private static final List<String> SUCCESS_MARKERS = Arrays.asList(
            "Preparing spawn area",
            "Done (",
            "Loaded world"
    );
    private static final List<String> FAILURE_MARKERS = Arrays.asList(
            "Encountered an unexpected exception",
            "Exception",
            "ERROR"
    );

    private AutoBisectRunner() {}

    public static boolean maybeRun(Path serverRoot) {
        if (System.getenv(CHILD_ENV) != null) {
            return false;
        }

        AutoBisectConfig config = AutoBisectConfig.load(serverRoot);
        if (!config.enabled) {
            return false;
        }

        if (isBlank(config.launchCmd)) {
            LOGGER.warn(DIAGNOSTICS, "Auto-bisect enabled but launchCmd is empty.");
            return false;
        }

        try {
            runBisect(serverRoot, config);
        } catch (Exception ex) {
            LOGGER.error(DIAGNOSTICS, "Auto-bisect failed.", ex);
        }
        return true;
    }

    private static void runBisect(Path serverRoot, AutoBisectConfig config) throws IOException, InterruptedException {
        Path modsDir = isBlank(config.modsDir)
                ? FMLPaths.MODSDIR.get()
                : serverRoot.resolve(config.modsDir);
        Path tempRoot = serverRoot.resolve(config.tempRoot);
        Files.createDirectories(tempRoot);

        List<Path> allMods = listMods(modsDir);
        Set<String> baselineNames = new HashSet<>(config.baseline);
        List<Path> baseline = allMods.stream()
                .filter(mod -> baselineNames.contains(mod.getFileName().toString()))
                .collect(Collectors.toList());
        List<Path> candidates = allMods.stream()
                .filter(mod -> !baselineNames.contains(mod.getFileName().toString()))
                .collect(Collectors.toList());

        int runId = 1;
        if (!runTest(serverRoot, tempRoot, baseline, Collections.<Path>emptyList(), config, runId++)) {
            LOGGER.error(DIAGNOSTICS, "Baseline mods already fail; bisect cannot proceed.");
            return;
        }

        List<Path> minimalFailing = new ArrayList<>(candidates);
        List<Path> lastPassing = Collections.emptyList();

        while (minimalFailing.size() > 1) {
            int mid = minimalFailing.size() / 2;
            List<Path> left = minimalFailing.subList(0, mid);
            List<Path> right = minimalFailing.subList(mid, minimalFailing.size());

            if (runTest(serverRoot, tempRoot, baseline, left, config, runId++)) {
                lastPassing = new ArrayList<>(left);
                minimalFailing = new ArrayList<>(right);
            } else {
                minimalFailing = new ArrayList<>(left);
            }
        }

        LOGGER.info(DIAGNOSTICS, "Auto-bisect results:");
        LOGGER.info(DIAGNOSTICS, "Baseline mods: {}", summarize(baseline));
        LOGGER.info(DIAGNOSTICS, "Minimal failing set: {}", summarize(minimalFailing));
        LOGGER.info(DIAGNOSTICS, "Last passing set: {}", summarize(lastPassing));
    }

    private static List<Path> listMods(Path modsDir) throws IOException {
        try (Stream<Path> stream = Files.list(modsDir)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .collect(Collectors.toList());
        }
    }

    private static boolean runTest(
            Path serverRoot,
            Path tempRoot,
            List<Path> baseline,
            List<Path> subset,
            AutoBisectConfig config,
            int runId
    ) throws IOException, InterruptedException {
        Path runDir = tempRoot.resolve(String.format("run_%03d", runId));
        if (Files.exists(runDir)) {
            deleteTree(runDir);
        }
        Files.createDirectories(runDir);

        Path gameDir = runDir.resolve("game");
        Path modsDir = gameDir.resolve("mods");
        Files.createDirectories(modsDir);

        for (Path mod : concat(baseline, subset)) {
            Files.copy(mod, modsDir.resolve(mod.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }

        for (String dirName : config.copyDirs) {
            Path src = serverRoot.resolve(dirName);
            if (Files.exists(src)) {
                copyTree(src, gameDir.resolve(dirName));
            }
        }

        String launchCmd = config.launchCmd
                .replace("{gamedir}", gameDir.toString())
                .replace("{modsdir}", modsDir.toString());

        ProcessBuilder builder = new ProcessBuilder("bash", "-lc", launchCmd);
        builder.directory(gameDir.toFile());
        builder.environment().put(CHILD_ENV, "1");
        Process process = builder.start();
        int exitCode = process.waitFor();

        Path logPath = gameDir.resolve("logs").resolve("latest.log");
        List<String> lines = Files.exists(logPath)
                ? Files.readAllLines(logPath)
                : Collections.<String>emptyList();

        boolean success = hasSuccess(lines);
        boolean failure = hasFailure(lines) || exitCode != 0;

        if (!success || failure) {
            List<String> context = extractFailureContext(lines, config.maxLogLines);
            LOGGER.error(DIAGNOSTICS, "Bisect run {} failed.", runId);
            for (String line : context) {
                LOGGER.error(DIAGNOSTICS, line);
            }
            return false;
        }
        return true;
    }

    private static boolean hasSuccess(List<String> lines) {
        boolean hasSpawn = lines.stream().anyMatch(line -> line.contains("Preparing spawn area"));
        boolean hasDone = lines.stream().anyMatch(line -> line.contains("Done ("));
        boolean hasLoaded = lines.stream().anyMatch(line -> line.contains("Loaded world"));
        return (hasSpawn && hasDone) || hasLoaded;
    }

    private static boolean hasFailure(List<String> lines) {
        return lines.stream().anyMatch(line -> FAILURE_MARKERS.stream().anyMatch(line::contains));
    }

    private static List<String> extractFailureContext(List<String> lines, int maxLines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (FAILURE_MARKERS.stream().anyMatch(line::contains)) {
                int start = Math.max(0, i - maxLines / 2);
                int end = Math.min(lines.size(), start + maxLines);
                return lines.subList(start, end);
            }
        }
        if (lines.size() <= maxLines) {
            return lines;
        }
        return lines.subList(lines.size() - maxLines, lines.size());
    }

    private static List<Path> concat(List<Path> left, List<Path> right) {
        List<Path> result = new ArrayList<>(left.size() + right.size());
        result.addAll(left);
        result.addAll(right);
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void copyTree(Path src, Path dst) throws IOException {
        if (Files.isDirectory(src)) {
            Files.createDirectories(dst);
        }
        try (Stream<Path> paths = Files.walk(src)) {
            for (Path path : paths.collect(Collectors.toList())) {
                Path target = dst.resolve(src.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static String summarize(List<Path> mods) {
        if (mods == null || mods.isEmpty()) {
            return "(none)";
        }
        return mods.stream()
                .map(path -> path.getFileName().toString())
                .collect(Collectors.joining(", "));
    }
}
