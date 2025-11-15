package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Guards the Mineralogy/ores sidemod entry point. */
public final class HellasAPIControlMineralogy {

    private HellasAPIControlMineralogy() {}

    /** Requires the "mineralogy" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("mineralogy");
    }
}
