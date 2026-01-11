package com.xsasakihaise.hellascontrol.api;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.client.ClientEnforcer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

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
        if (FMLEnvironment.dist.isDedicatedServer()) {
            HellasControl.requireEntitlement(entitlementKey);
        }
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    public static boolean getServerLicensedClientSide() {
        return HellasControl.isServerLicensedClientSide();
    }

    public static void requireServerLicensedClientSide() {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        if (!getServerLicensedClientSide()) {
            ClientEnforcer.disconnectForMissingServer(
                    "This server is not licensed to run HellasControl / Hephaestus Forge software.");
        }
    }
}
