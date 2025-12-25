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
        System.out.println("[HellasControl] LicenseEvents.onServerStart");
        if (!e.getServer().isDedicatedServer()) {
            return;
        }
        System.out.println("[HellasControl] License enforcement handled by HellasControl.onServerStart.");
    }
}
