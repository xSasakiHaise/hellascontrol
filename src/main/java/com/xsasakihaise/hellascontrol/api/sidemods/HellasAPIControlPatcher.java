package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Gatekeeper for the Hellas patcher/launcher integration module. */
public final class HellasAPIControlPatcher {

    private HellasAPIControlPatcher() {}

    /** Requires the "patcher" entitlement before initializing patcher hooks. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("patcher");
    }
}
