package com.xsasakihaise.hellascontrol.license;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the Hellas license state as read from disk or
 * received from the remote verification endpoint. Instances are shared by
 * {@link com.xsasakihaise.hellascontrol.HellasControl} and sidemods for
 * entitlement checks.
 */
public final class LicenseCache {
    private static final Gson GSON = new Gson();

    private boolean valid;
    private String licenseId;
    private String message;
    private String expires;
    private String serverUrl; // optional
    private List<String> entitlements = new ArrayList<>(); // <-- NEW

    public boolean isValid() { return valid; }
    public String getLicenseId() { return licenseId; }
    public String getMessage() { return message; }
    public String getExpires() { return expires; }
    public String getServerUrl() { return serverUrl; }
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
        c.valid = "valid".equalsIgnoreCase(r.getStatus());
        c.licenseId = r.getLicenseId();
        c.message = r.getMessage();
        c.expires = r.getExpires();
        c.entitlements = new ArrayList<>(r.getEntitlements() == null ? Collections.emptyList() : r.getEntitlements());
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache loaded from response status={}", r.getStatus());
        return c;
    }

    /**
     * Factory helper for an invalid cache with a descriptive message.
     */
    public static LicenseCache invalid(String msg) {
        LicenseCache c = new LicenseCache();
        c.valid = false;
        c.message = msg;
        c.entitlements = new ArrayList<>();
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache invalid: {}", msg);
        return c;
    }

    /**
     * Creates a cache entry from a plain-text license ID.
     */
    public static LicenseCache fromLicenseId(String licenseId) {
        LicenseCache c = new LicenseCache();
        c.valid = false;
        c.licenseId = licenseId == null ? "" : licenseId.trim();
        c.message = c.licenseId.isEmpty() ? "Missing license.txt entry" : "License loaded from license.txt";
        c.entitlements = new ArrayList<>();
        org.apache.logging.log4j.LogManager.getLogger(LicenseCache.class)
                .info("[HellasControl] LicenseCache created from license id");
        return c;
    }
}
