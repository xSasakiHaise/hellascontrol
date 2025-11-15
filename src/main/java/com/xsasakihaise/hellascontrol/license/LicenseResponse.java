package com.xsasakihaise.hellascontrol.license;

import java.util.List;

/**
 * POJO mirroring the JSON schema returned by the remote WordPress licensing
 * endpoint.
 */
public final class LicenseResponse {
    private String status;     // "valid" | "expired" | "revoked" | "not_found" | "mismatch"
    private String licenseId;
    private String message;
    private String expires;
    private List<String> entitlements; // <-- NEW

    public String getStatus() { return status; }
    public String getLicenseId() { return licenseId; }
    public String getMessage() { return message; }
    public String getExpires() { return expires; }
    public List<String> getEntitlements() { return entitlements; }

    /** Default constructor used by Gson. */
    public LicenseResponse() {}
}
