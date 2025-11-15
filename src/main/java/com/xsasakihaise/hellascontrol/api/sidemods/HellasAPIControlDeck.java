package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/**
 * Guard utility for the Deck-of-Mons sidemod. Ensures only licensed servers
 * initialize the module.
 */
public final class HellasAPIControlDeck {

    private HellasAPIControlDeck() {}

    /** Validates the presence of HellasControl and the "deck" entitlement. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("deck");
    }
}
