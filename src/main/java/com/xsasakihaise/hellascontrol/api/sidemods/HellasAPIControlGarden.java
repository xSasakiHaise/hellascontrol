package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Loader hook for the Hellas Garden content pack. */
public final class HellasAPIControlGarden {

    private HellasAPIControlGarden() {}

    /** Ensures the server license contains the "garden" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("garden");
    }
}
