package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Loader helper for the shared Hellas Library. */
public final class HellasAPIHellasLibrary {

    private HellasAPIHellasLibrary() {}

    /** Requires the "library" entitlement before exposing APIs. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("library");
    }
}
