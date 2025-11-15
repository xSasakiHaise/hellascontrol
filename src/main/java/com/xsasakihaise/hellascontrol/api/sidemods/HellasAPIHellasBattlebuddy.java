package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Verifier for the Hellas Battlebuddy assistant mod. */
public final class HellasAPIHellasBattlebuddy {

    private HellasAPIHellasBattlebuddy() {}

    /** Validates that the "battlebuddy" entitlement is granted. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("battlebuddy");
    }
}
