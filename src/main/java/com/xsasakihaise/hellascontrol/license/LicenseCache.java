package com.xsasakihaise.hellascontrol.license;

import com.google.gson.Gson;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Immutable snapshot of the Hellas license state as read from disk or
 * received from the remote verification endpoint. Instances are shared by
 * {@link com.xsasakihaise.hellascontrol.HellasControl} and sidemods for
 * entitlement checks.
 */
public final class LicenseCache {
    private static final Gson GSON = new Gson();
    private static final Duration ROTATION_OVERLAP = Duration.ofMinutes(15);

    @com.google.gson.annotations.SerializedName(value = "licensed", alternate = {"valid"})
    private boolean licensed;
    private String licenseId;
    private String message;
    private String expires;
    private String serverUrl; // optional
    private List<String> entitlements = new ArrayList<>(); // <-- NEW
    private String currentToken;
    private String nextToken;
    private Instant currentValidFrom;
    private Instant currentValidTo;
    private Instant nextValidFrom;
    private Instant nextValidTo;
    private Instant lastRefreshAttempt;
    private String serverFingerprint;

    public boolean isLicensed() { return licensed; }
    public boolean isValid() { return licensed; }
    public String getLicenseId() { return licenseId; }
    public String getMessage() { return message; }
    public String getExpires() { return expires; }
    public String getServerUrl() { return serverUrl; }
    public String getCurrentToken() { return currentToken; }
    public String getNextToken() { return nextToken; }
    public Instant getCurrentValidFrom() { return currentValidFrom; }
    public Instant getCurrentValidTo() { return currentValidTo; }
    public Instant getNextValidFrom() { return nextValidFrom; }
    public Instant getNextValidTo() { return nextValidTo; }
    public Instant getLastRefreshAttempt() { return lastRefreshAttempt; }
    public String getServerFingerprint() { return serverFingerprint; }
    /**
     * @return immutable list of entitlements granted by the license
     */
    public List<String> getEntitlements() {
        return entitlements == null ? Collections.emptyList() : Collections.unmodifiableList(entitlements);
    }

    /**
     * Parses the cached {@code license.json} stored on the server. Returns an
     * invalid cache if the file is corrupt.
     */
    public static LicenseCache fromJson(String json) {
        try {
            LicenseCache c = GSON.fromJson(json, LicenseCache.class);
            if (c.entitlements == null) c.entitlements = new ArrayList<>();
            c.lastRefreshAttempt = Instant.now();
            c.serverFingerprint = c.computeFingerprint();
            org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                    .info("[HellasControl] LicenseCache loaded from json");
            return c;
        } catch (Exception e) {
            return invalid("Corrupt license.json");
        }
    }

    /**
     * Converts a {@link LicenseResponse} from the remote API to a cache entry
     * that can be stored locally.
     */
    public static LicenseCache fromResponse(LicenseResponse r) {
        LicenseCache c = new LicenseCache();
        c.licensed = "valid".equalsIgnoreCase(r.getStatus());
        c.licenseId = r.getLicenseId();
        c.message = r.getMessage();
        c.expires = r.getExpires();
        c.entitlements = new ArrayList<>(r.getEntitlements() == null ? Collections.emptyList() : r.getEntitlements());
        c.currentToken = normalizeToken(r.getCurrentToken());
        c.nextToken = normalizeToken(r.getNextToken());
        c.currentValidFrom = parseInstant(r.getCurrentValidFrom());
        c.currentValidTo = parseInstant(r.getCurrentValidTo());
        c.nextValidFrom = parseInstant(r.getNextValidFrom());
        c.nextValidTo = parseInstant(r.getNextValidTo());
        c.lastRefreshAttempt = Instant.now();
        c.serverFingerprint = c.computeFingerprint();
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache loaded from response status={}", r.getStatus());
        return c;
    }

    /**
     * Factory helper for an invalid cache with a descriptive message.
     */
    public static LicenseCache invalid(String msg) {
        LicenseCache c = new LicenseCache();
        c.licensed = false;
        c.message = msg;
        c.entitlements = new ArrayList<>();
        c.lastRefreshAttempt = Instant.now();
        c.serverFingerprint = c.computeFingerprint();
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache invalid: {}", msg);
        return c;
    }

    /**
     * Creates a cache entry from a plain-text license ID.
     */
    public static LicenseCache fromLicenseId(String licenseId) {
        LicenseCache c = new LicenseCache();
        c.licensed = false;
        c.licenseId = licenseId == null ? "" : licenseId.trim();
        c.message = c.licenseId.isEmpty() ? "Missing license.txt entry" : "License loaded from license.txt";
        c.entitlements = new ArrayList<>();
        c.lastRefreshAttempt = Instant.now();
        c.serverFingerprint = c.computeFingerprint();
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache created from license id");
        return c;
    }

    public boolean isCurrentTokenValid(Instant now) {
        return isTokenValid(now, currentToken, currentValidFrom, currentValidTo);
    }

    public boolean isNextTokenValid(Instant now) {
        return isTokenValid(now, nextToken, nextValidFrom, nextValidTo);
    }

    public boolean isExpiredWithoutNext(Instant now) {
        if (now == null) {
            now = Instant.now();
        }
        boolean currentExpired = currentToken == null || currentToken.isEmpty()
                || (currentValidTo != null && now.isAfter(currentValidTo.plus(ROTATION_OVERLAP)));
        boolean nextAvailable = nextToken != null && !nextToken.isEmpty()
                && (nextValidTo == null || now.isBefore(nextValidTo.plus(ROTATION_OVERLAP)));
        return currentExpired && !nextAvailable;
    }

    public LicenseCache withLastRefreshAttempt(Instant instant) {
        LicenseCache c = new LicenseCache();
        c.licensed = this.licensed;
        c.licenseId = this.licenseId;
        c.message = this.message;
        c.expires = this.expires;
        c.serverUrl = this.serverUrl;
        c.entitlements = new ArrayList<>(this.entitlements == null ? Collections.emptyList() : this.entitlements);
        c.currentToken = this.currentToken;
        c.nextToken = this.nextToken;
        c.currentValidFrom = this.currentValidFrom;
        c.currentValidTo = this.currentValidTo;
        c.nextValidFrom = this.nextValidFrom;
        c.nextValidTo = this.nextValidTo;
        c.lastRefreshAttempt = instant;
        c.serverFingerprint = this.serverFingerprint;
        return c;
    }

    private static boolean isTokenValid(Instant now, String token, Instant validFrom, Instant validTo) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        if (now == null) {
            now = Instant.now();
        }
        Instant start = validFrom == null ? null : validFrom.minus(ROTATION_OVERLAP);
        Instant end = validTo == null ? null : validTo.plus(ROTATION_OVERLAP);
        if (start != null && now.isBefore(start)) {
            return false;
        }
        if (end != null && now.isAfter(end)) {
            return false;
        }
        return true;
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String computeFingerprint() {
        List<String> sorted = getEntitlements().stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .sorted()
                .collect(Collectors.toList());
        String raw = String.format("%s|%s|%s", licenseId == null ? "" : licenseId,
                expires == null ? "" : expires, String.join(",", sorted));
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
