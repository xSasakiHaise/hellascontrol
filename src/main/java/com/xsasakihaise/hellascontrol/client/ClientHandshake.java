package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.network.ModPing;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import com.xsasakihaise.hellascontrol.ClientModState;
import com.xsasakihaise.hellascontrol.HellasControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Client-side subscriber that initiates the license handshake when logging in. */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientHandshake {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandshake.class);

    private ClientHandshake() {}

    /** Fired after the client has logged into a server (remote or integrated). */
    @SubscribeEvent
    public static void onClientLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent e) {
        LOGGER.info("[HellasControl] ClientHandshake.onClientLoggedIn");
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        ClientModState.beginHandshake();
        if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
            LOGGER.info("[HellasControl] Sending handshake ping to remote server.");
        }
        // Ping the server once to ask: “Do you have HellasControl, and is it licensed?”
        NetworkHandler.CHANNEL.sendToServer(new ModPing());
    }

    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        LOGGER.info("[HellasControl] ClientHandshake.onClientLoggedOut");
        ClientModState.clear();
    }
}
