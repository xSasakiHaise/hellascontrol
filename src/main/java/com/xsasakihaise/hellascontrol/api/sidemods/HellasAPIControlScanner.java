package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/**
 * Guard helper for the Hellas scanner sidemod. Call {@link #verify()} during
 * setup to ensure the core mod is loaded and the server license grants scanner access.
 */
public final class HellasAPIControlScanner {

    private HellasAPIControlScanner() {}

    /** Ensures the core mod is present and that the "scanner" entitlement exists. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("scanner");
    }
}
