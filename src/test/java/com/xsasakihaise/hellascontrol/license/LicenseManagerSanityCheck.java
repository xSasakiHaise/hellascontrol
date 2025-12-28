package com.xsasakihaise.hellascontrol.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class LicenseManagerSanityCheck {
    private LicenseManagerSanityCheck() {}

    public static void main(String[] args) throws IOException {
        Path missingRoot = Files.createTempDirectory("hellascontrol-missing");
        Path missingLicense = LicenseManager.ensureLicenseFile(missingRoot);
        if (!Files.exists(missingLicense)) {
            throw new AssertionError("Expected license.txt to be created");
        }
        LicenseManager.initialize(missingRoot);
        assertInvalidWithMessage("License id empty", LicenseManager.getCached());

        Path emptyRoot = Files.createTempDirectory("hellascontrol-empty");
        Path emptyLicense = emptyRoot.resolve("config").resolve("hellascontrol").resolve("license.txt");
        Files.createDirectories(emptyLicense.getParent());
        Files.write(emptyLicense, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LicenseManager.initialize(emptyRoot);
        assertInvalidWithMessage("License id empty", LicenseManager.getCached());

        Path validRoot = Files.createTempDirectory("hellascontrol-valid");
        Path validLicense = validRoot.resolve("config").resolve("hellascontrol").resolve("license.txt");
        Files.createDirectories(validLicense.getParent());
        Files.write(validLicense, "ABC-123".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LicenseManager.initialize(validRoot);
        LicenseCache cached = LicenseManager.getCached();
        if (cached == null || cached.getLicenseId() == null || !cached.getLicenseId().equals("ABC-123")) {
            throw new AssertionError("Expected license id to load from license.txt");
        }
    }

    private static void assertInvalidWithMessage(String message, LicenseCache cache) {
        if (cache == null) {
            throw new AssertionError("Expected cache instance");
        }
        if (cache.isValid()) {
            throw new AssertionError("Expected cache to be invalid");
        }
        if (cache.getMessage() == null || !cache.getMessage().equals(message)) {
            throw new AssertionError("Expected cache message '" + message + "'");
        }
    }
}
