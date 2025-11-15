package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/**
 * Entry point helper for sidemods that depend on the Elo ranking module.
 */
public final class HellasAPIControlElo {

    private HellasAPIControlElo() {}

    /** Validates access to the "elo" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("elo");
    }
}
