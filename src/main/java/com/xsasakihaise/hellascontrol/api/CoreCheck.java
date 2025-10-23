package com.xsasakihaise.hellascontrol.api;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.xsasakihaise.hellascontrol.HellasControl;

/**
 * Shared verification for all sidemods.
 * - Presence check (both sides)
 * - Entitlement check (server side only)
 */
public final class CoreCheck {
    private static final String CORE_MODID = "hellascontrol";
    private CoreCheck() {}

    /** Call in sidemod constructor or FMLCommonSetupEvent. */
    public static void verifyCoreLoaded() {
        if (!ModList.get().isLoaded(CORE_MODID))
            throw new IllegalStateException("HellasControl core mod missing!");
    }

    /** SERVER-ONLY: require a named entitlement (e.g., "forms", "garden"). */
    public static void verifyEntitled(String entitlementKey) {
        if (!FMLEnvironment.dist.isDedicatedServer()) {
            // Ignore on client/integrated to avoid false crashes; server enforces.
            return;
        }
        HellasControl.requireEntitlement(entitlementKey);
    }
}
