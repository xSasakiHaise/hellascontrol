package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Wraps entitlement checks for the Hellas textures pack. */
public final class HellasAPIHellasTextures {

    private HellasAPIHellasTextures() {}

    /** Requires the "textures" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("textures");
    }
}
