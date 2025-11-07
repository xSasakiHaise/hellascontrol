package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

public final class HellasAPIHellasBattlebuddy {

    private HellasAPIHellasBattlebuddy() {}

    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("battlebuddy");
    }
}
