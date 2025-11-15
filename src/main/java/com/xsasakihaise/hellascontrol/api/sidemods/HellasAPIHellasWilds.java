package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Ensures Hellas Wilds content loads only on licensed servers. */
public final class HellasAPIHellasWilds {

    private HellasAPIHellasWilds() {}

    /** Requires the "wilds" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("wilds");
    }
}
