package com.xsasakihaise.hellascontrol.license;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

/**
 * Talks to your WordPress-based license API.
 * Endpoint: https://web.hephaestus-forge.cc/wp-json/hellas/v1/license/verify
 *
 * NOTE:
 * - Server-only: call from LicenseManager.verifyServer()
 * - We persist a machineId in config/hellas/machine.id (UUID) on first run
 * - Expects JSON with: status, licenseId, message, expires, entitlements[], signature
 */
public final class LicenseServerClient {

    // If you later want to make this configurable, read it from license.json and fall back to this default.
    private static final String VERIFY_URL =
            "https://web.hephaestus-forge.cc/wp-json/hellas/v1/license/verify";

    private static final Gson GSON = new Gson();

    private LicenseServerClient() {}

    /**
     * POST the current license to the server and parse response.
     * Returns Optional.empty() on network/parse errors (caller should keep local cache state).
     */
    public static Optional<LicenseResponse> verifyRemote(LicenseCache current) {
        try {
            String licenseId = (current != null && current.getLicenseId() != null)
                    ? current.getLicenseId().trim()
                    : "";

            if (licenseId.isEmpty()) {
                return Optional.empty(); // nothing to verify yet
            }

            String machineId = ensureMachineId(); // persisted UUID on first use

            JsonObject body = new JsonObject();
            body.addProperty("licenseId", licenseId);
            body.addProperty("machineId", machineId);
            // Optional extras (WP endpoint ignores if unused)
            body.addProperty("version", "1.0.0");
            // body.add("modList", new JsonArray()); // not needed now

            HttpURLConnection conn = (HttpURLConnection) new URL(VERIFY_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(7000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                // Best-effort: read error stream to aid debugging
                try (InputStream es = conn.getErrorStream()) {
                    if (es != null) {
                        byte[] err = readAll(es);
                        System.err.println("[HellasControl] LicenseServer error " + code + ": " + new String(err, StandardCharsets.UTF_8));
                    } else {
                        System.err.println("[HellasControl] LicenseServer HTTP " + code);
                    }
                }
                return Optional.empty();
            }

            try (InputStream is = conn.getInputStream();
                 Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                LicenseResponse resp = GSON.fromJson(reader, LicenseResponse.class);
                if (resp == null || resp.getStatus() == null) {
                    return Optional.empty();
                }
                // Optional: verify HMAC signature here if you want (the WP plugin returns "signature")
                // You'd need to share the secret or fetch it via a secure method; skipping for now.
                return Optional.of(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Persist and return a stable machine ID under {@code config/hellas/machine.id}.
     * The identifier is required by the WP endpoint to bind licenses to hosts.
     */
    private static String ensureMachineId() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("hellas");
        Path file = configDir.resolve("machine.id");
        try {
            Files.createDirectories(configDir);
            if (Files.exists(file)) {
                String v = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
                if (!v.isEmpty()) return v;
            }
            String generated = UUID.randomUUID().toString();
            Files.write(file, generated.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return generated;
        } catch (IOException io) {
            // As a last resort, return a volatile UUID (won't bind properly)
            System.err.println("[HellasControl] Failed to persist machine.id, using volatile UUID");
            return UUID.randomUUID().toString();
        }
    }

    /** Utility to drain an InputStream into a byte array. */
    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
        return baos.toByteArray();
    }
}
