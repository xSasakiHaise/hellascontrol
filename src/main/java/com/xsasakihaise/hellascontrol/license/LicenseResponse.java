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
    private String currentToken;
    private String nextToken;
    private String currentValidFrom;
    private String currentValidTo;
    private String nextValidFrom;
    private String nextValidTo;

    public String getStatus() { return status; }
    public String getLicenseId() { return licenseId; }
    public String getMessage() { return message; }
    public String getExpires() { return expires; }
    public List<String> getEntitlements() { return entitlements; }
    public String getCurrentToken() { return currentToken; }
    public String getNextToken() { return nextToken; }
    public String getCurrentValidFrom() { return currentValidFrom; }
    public String getCurrentValidTo() { return currentValidTo; }
    public String getNextValidFrom() { return nextValidFrom; }
    public String getNextValidTo() { return nextValidTo; }

    /** Default constructor used by Gson. */
    public LicenseResponse() {}
}
