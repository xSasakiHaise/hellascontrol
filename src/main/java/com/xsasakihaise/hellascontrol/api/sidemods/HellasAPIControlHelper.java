package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Protects the Helper/utility sidemod behind its license entitlement. */
public final class HellasAPIControlHelper {

    private HellasAPIControlHelper() {}

    /** Validates the "helper" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("helper");
    }
}
