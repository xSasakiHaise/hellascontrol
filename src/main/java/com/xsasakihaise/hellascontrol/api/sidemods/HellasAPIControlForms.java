package com.xsasakihaise.hellascontrol.api.sidemods;

import com.xsasakihaise.hellascontrol.api.CoreCheck;

/** Utility class for sidemods adding custom forms. */
public final class HellasAPIControlForms {
    private HellasAPIControlForms() {}

    /** Ensures the "forms" entitlement exists before loading content. */
    public static void verify() {
        CoreCheck.verifyCoreLoaded();
        CoreCheck.verifyEntitled("forms");
    }
}
