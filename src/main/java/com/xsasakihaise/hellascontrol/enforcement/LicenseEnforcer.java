package com.xsasakihaise.hellascontrol.enforcement;

import com.xsasakihaise.hellascontrol.license.LicenseManager;

/**
 * Performs the actual license verification and exposes the result for other
 * systems such as the network handshake.
 */
public final class LicenseEnforcer {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(LicenseEnforcer.class);
    private static volatile boolean serverLicensed = false;

    private LicenseEnforcer() {}

    /**
     * Runs the {@link LicenseManager} verification and remembers the result.
     *
     * @return {@code true} if the license is valid according to the last check
     */
    public static boolean enforceServerLicense() {
        LOGGER.info("[HellasControl] LicenseEnforcer.enforceServerLicense");
        serverLicensed = LicenseManager.verifyServer();
        System.out.println("[HellasControl] Server license status: " + (serverLicensed ? "VALID" : "INVALID"));
        return serverLicensed;
    }

    /**
     * @return the most recent outcome of {@link #enforceServerLicense()}
     */
    public static boolean isServerLicensed() {
        return serverLicensed;
    }
}
