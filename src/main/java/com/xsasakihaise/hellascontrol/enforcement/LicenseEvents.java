package com.xsasakihaise.hellascontrol.enforcement;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

/**
 * Forge event subscriber that runs the license enforcement when a dedicated
 * server starts.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LicenseEvents {

    @SubscribeEvent
    public static void onServerStart(FMLServerStartingEvent e) {
        try {
            boolean ok = LicenseEnforcer.enforceServerLicense();
            if (ok) {
                System.out.println("[HellasControl] License validated successfully.");
            } else {
                System.err.println("[HellasControl] License validation failed or missing.");
            }
        } catch (Exception ex) {
            System.err.println("[HellasControl] License validation error: " + ex.getMessage());
        }
    }
}
