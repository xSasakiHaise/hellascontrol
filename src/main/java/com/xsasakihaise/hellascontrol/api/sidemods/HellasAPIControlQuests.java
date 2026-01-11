package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/**
 * Guard helper for the Hellas quests sidemod. Call {@link #verify()} during
 * setup to ensure the core mod is loaded and the server license grants quests.
 */
public final class HellasAPIControlQuests {

    private HellasAPIControlQuests() {}

    /** Ensures the core mod is present and that the "quests" entitlement exists. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("quests");
    }
}
