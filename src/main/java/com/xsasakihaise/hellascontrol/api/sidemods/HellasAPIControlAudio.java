package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/**
 * Loader-facing guard for the Hellas Audio sidemod. Call
 * {@link #verify()} during your mod setup to ensure the core module is
 * present and the server's license grants access to audio features.
 */
public final class HellasAPIControlAudio {

    private HellasAPIControlAudio() {}

    /** Ensures the core mod is present and that the "audio" entitlement exists. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("audio");
    }
}
