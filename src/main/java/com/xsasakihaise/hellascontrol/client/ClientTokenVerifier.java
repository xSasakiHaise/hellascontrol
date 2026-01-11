package com.xsasakihaise.hellascontrol.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ClientTokenVerifier {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientTokenVerifier.class);
    private static final Gson GSON = new Gson();
    private static final String VERIFY_URL =
            "https://web.hephaestus-forge.cc/wp-json/hellas/v1/license/validate-token";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "hellascontrol-client-token-verify");
        t.setDaemon(true);
        return t;
    });
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();

    private ClientTokenVerifier() {}

    public static void verifyTokenAsync() {
        if (ClientModState.isTokenValidationInFlight() || ClientModState.isTokenValidationComplete()) {
            return;
        }
        String serverId = resolveServerId();
        if (serverId == null || serverId.isEmpty()) {
            ClientModState.markTokenValidationResult(false);
            ClientEnforcer.disconnectForInvalidToken(
                    "This server is not licensed to run HellasControl / Hephaestus Forge software.");
            return;
        }
        CacheEntry cached = CACHE.get(serverId);
        if (cached != null && !cached.isExpired()) {
            ClientModState.markTokenValidationResult(cached.valid);
            if (!cached.valid) {
                ClientEnforcer.disconnectForInvalidToken(
                        "This server is not licensed to run HellasControl / Hephaestus Forge software.");
            }
            return;
        }
        String currentToken = ClientModState.getServerToken();
        String nextToken = ClientModState.getServerTokenNext();
        if ((currentToken == null || currentToken.isEmpty())
                && (nextToken == null || nextToken.isEmpty())) {
            ClientModState.markTokenValidationResult(false);
            ClientEnforcer.disconnectForInvalidToken(
                    "This server did not provide a validation token.");
            return;
        }
        ClientModState.markTokenValidationInFlight();
        CompletableFuture.supplyAsync(() -> verifyToken(currentToken, nextToken), EXECUTOR)
                .thenAccept(valid -> {
                    CACHE.put(serverId, new CacheEntry(valid, Instant.now()));
                    ClientModState.markTokenValidationResult(valid);
                    if (!valid) {
                        ClientEnforcer.disconnectForInvalidToken(
                                "This server is not licensed to run HellasControl / Hephaestus Forge software.");
                    }
                });
    }

    private static boolean verifyToken(String currentToken, String nextToken) {
        if (currentToken != null && !currentToken.isEmpty()) {
            if (verifySingleToken(currentToken)) {
                return true;
            }
        }
        if (nextToken != null && !nextToken.isEmpty()) {
            return verifySingleToken(nextToken);
        }
        return false;
    }

    private static boolean verifySingleToken(String token) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(VERIFY_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            JsonObject payload = new JsonObject();
            payload.addProperty("token", token);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                LOGGER.info("[HellasControl] Client token verify HTTP {}", code);
                return false;
            }

            try (InputStream is = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                JsonObject resp = GSON.fromJson(reader, JsonObject.class);
                if (resp == null || !resp.has("valid")) {
                    return false;
                }
                return resp.get("valid").getAsBoolean();
            }
        } catch (Exception e) {
            LOGGER.info("[HellasControl] Client token verify failed: {}", e.getMessage());
            return false;
        }
    }

    private static String resolveServerId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getCurrentServer() == null) {
            return "";
        }
        ServerData data = mc.getCurrentServer();
        return data == null ? "" : data.ip;
    }

    private static final class CacheEntry {
        private final boolean valid;
        private final Instant checkedAt;

        private CacheEntry(boolean valid, Instant checkedAt) {
            this.valid = valid;
            this.checkedAt = checkedAt;
        }

        private boolean isExpired() {
            return checkedAt.plus(CACHE_TTL).isBefore(Instant.now());
        }
    }
}
