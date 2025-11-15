package com.xsasakihaise.hellascontrol.license;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;

/**
 * Coordinates reading the on-disk license file and optionally verifying it
 * with the remote licensing API. Other systems query this class for the last
 * cached {@link LicenseCache}.
 */
public final class LicenseManager {

    private static LicenseCache cached = LicenseCache.invalid("Uninitialized");
    private static Path configDir;

    private LicenseManager() {}

    /**
     * Initializes the license manager for the provided server root. The method
     * creates {@code config/hellas/license.json} if missing and populates the
     * {@link #cached} snapshot.
     */
    public static void initialize(Path serverRoot) {
        // serverRoot already a Path — do NOT .toPath()
        Path rootConfig = (serverRoot != null)
                ? serverRoot.resolve("config")
                : FMLPaths.CONFIGDIR.get();

        configDir = rootConfig.resolve("hellas");
        try {
            Files.createDirectories(configDir);
        } catch (IOException ignored) {}

        Path licenseFile = configDir.resolve("license.json");
        if (Files.exists(licenseFile)) {
            try {
                String json = new String(Files.readAllBytes(licenseFile), StandardCharsets.UTF_8);
                cached = LicenseCache.fromJson(json);
            } catch (Exception e) {
                e.printStackTrace();
                cached = LicenseCache.invalid("Failed to read license.json");
            }
        } else {
            cached = LicenseCache.invalid("No license.json found");
        }
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
}
