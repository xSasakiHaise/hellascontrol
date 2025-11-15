package com.xsasakihaise.hellascontrol.api;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.xsasakihaise.hellascontrol.HellasControl;

/**
 * Shared verification helpers for all Hellas sidemods. The methods are meant
 * to be invoked from each sidemod's constructor or setup handler so they fail
 * fast whenever the core control mod is missing or the server lacks the
 * entitlement that unlocks that sidemod.
 */
public final class CoreCheck {
    private static final String CORE_MODID = "hellascontrol";
    private CoreCheck() {}

    /** Call in sidemod constructor or FMLCommonSetupEvent. */
    public static void verifyCoreLoaded() {
        if (!ModList.get().isLoaded(CORE_MODID))
            throw new IllegalStateException("HellasControl core mod missing!");
    }

    /**
     * SERVER-ONLY: require a named entitlement (e.g., "forms", "garden").
     * The check is intentionally skipped on the physical client to avoid
     * crashing a player who connects to an unlicensed server.
     */
    public static void verifyEntitled(String entitlementKey) {
        if (!FMLEnvironment.dist.isDedicatedServer()) {
            // Ignore on client/integrated to avoid false crashes; server enforces.
            return;
        }
        HellasControl.requireEntitlement(entitlementKey);
    }
}
