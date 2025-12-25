package com.xsasakihaise.hellascontrol.license;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates reading the on-disk license file and optionally verifying it
 * with the remote licensing API. Other systems query this class for the last
 * cached {@link LicenseCache}.
 */
public final class LicenseManager {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(LicenseManager.class);

    private static LicenseCache cached = LicenseCache.invalid("Uninitialized");
    private static Path configDir;
    private static Path licenseFile;

    private LicenseManager() {}

    /**
     * Initializes the license manager for the provided server root. The method
     * creates {@code config/hellascontrol/license.txt} if missing and populates the
     * {@link #cached} snapshot.
     */
    public static void initialize(Path serverRoot) {
        LOGGER.info("[HellasControl] LicenseManager.initialize serverRoot={}", serverRoot);
        // serverRoot already a Path — do NOT .toPath()
        Path rootConfig = (serverRoot != null)
                ? serverRoot.resolve("config")
                : FMLPaths.CONFIGDIR.get();

        configDir = rootConfig.resolve("hellascontrol");
        LOGGER.info("[HellasControl] LicenseManager configDir={}", configDir);
        try {
            Files.createDirectories(configDir);
        } catch (IOException ignored) {}

        licenseFile = configDir.resolve("license.txt");
        LOGGER.info("[HellasControl] LicenseManager licenseFile={}", licenseFile);
        String licenseId = readLicenseId(licenseFile);
        if (!licenseId.isEmpty()) {
            LOGGER.info("[HellasControl] LicenseManager loaded license id from license.txt");
            cached = LicenseCache.fromLicenseId(licenseId);
            return;
        }

        Path legacyFile = rootConfig.resolve("hellas").resolve("license.json");
        LOGGER.info("[HellasControl] LicenseManager legacyFile={}", legacyFile);
        if (Files.exists(legacyFile)) {
            try {
                String json = new String(Files.readAllBytes(legacyFile), StandardCharsets.UTF_8);
                LOGGER.info("[HellasControl] LicenseManager loaded legacy license.json");
                cached = LicenseCache.fromJson(json);
            } catch (Exception e) {
                e.printStackTrace();
                cached = LicenseCache.invalid("Failed to read legacy license.json");
            }
        } else {
            LOGGER.info("[HellasControl] LicenseManager did not find license.txt or legacy license.json");
            cached = LicenseCache.invalid("No license.txt found");
        }
    }

    public static Path ensureLicenseFile(Path serverRoot) {
        LOGGER.info("[HellasControl] LicenseManager.ensureLicenseFile serverRoot={}", serverRoot);
        Path rootConfig = (serverRoot != null)
                ? serverRoot.resolve("config")
                : FMLPaths.CONFIGDIR.get();
        Path dir = rootConfig.resolve("hellascontrol");
        Path file = dir.resolve("license.txt");
        try {
            Files.createDirectories(dir);
            if (!Files.exists(file)) {
                LOGGER.info("[HellasControl] LicenseManager creating license.txt at {}", file);
                List<String> lines = java.util.Arrays.asList(
                        "# HellasControl license",
                        "# Paste your license id on a single line below.",
                        ""
                );
                Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            } else {
                LOGGER.info("[HellasControl] LicenseManager license.txt already exists at {}", file);
            }
        } catch (IOException ignored) {}
        return file;
    }

    /**
     * Attempts to validate the cached license. On failure it makes a best
     * effort call to the remote verification endpoint.
     *
     * @return {@code true} if the server currently holds a valid license
     */
    public static boolean verifyServer() {
        if (cached.isValid()) return true;
        Optional<LicenseResponse> resp = LicenseServerClient.verifyRemote(cached);
        if (resp.isPresent()) {
            cached = LicenseCache.fromResponse(resp.get());
        }
        return cached.isValid();
    }

    /**
     * @return latest cached license snapshot
     */
    public static LicenseCache getCached() { return cached; }

    private static String readLicenseId(Path file) {
        if (file == null || !Files.exists(file)) {
            LOGGER.info("[HellasControl] LicenseManager readLicenseId skipped (missing file) {}", file);
            return "";
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null) {
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                    continue;
                }
                LOGGER.info("[HellasControl] LicenseManager readLicenseId found license id");
                return trimmed;
            }
        } catch (IOException ignored) {}
        LOGGER.info("[HellasControl] LicenseManager readLicenseId found no license id");
        return "";
    }
}
