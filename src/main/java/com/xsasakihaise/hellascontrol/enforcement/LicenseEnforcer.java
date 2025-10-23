package com.xsasakihaise.hellascontrol.enforcement;

import com.xsasakihaise.hellascontrol.license.LicenseManager;

public final class LicenseEnforcer {
    private static volatile boolean serverLicensed = false;

    private LicenseEnforcer() {}

    public static boolean enforceServerLicense() {
        serverLicensed = LicenseManager.verifyServer();
        System.out.println("[HellasControl] Server license status: " + (serverLicensed ? "VALID" : "INVALID"));
        return serverLicensed;
    }

    public static boolean isServerLicensed() {
        return serverLicensed;
    }
}
