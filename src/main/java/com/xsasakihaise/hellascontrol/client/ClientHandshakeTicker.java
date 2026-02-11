package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientHandshakeTicker {
    private static final long HANDSHAKE_TIMEOUT_MS = 2500L;
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientHandshakeTicker.class);
    private static boolean wasRemoteConnected = false;

    private ClientHandshakeTicker() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean isRemoteConnected = ClientConnectionUtil.isRemoteConnection(mc);

        if (isRemoteConnected && !wasRemoteConnected) {
            ClientHandshake.onClientLoggedIn();
        } else if (!isRemoteConnected && wasRemoteConnected) {
            ClientHandshake.onClientLoggedOut();
        }
        wasRemoteConnected = isRemoteConnected;

        if (!ClientModState.isHandshakePending() || !isRemoteConnected) {
            return;
        }
        long elapsed = System.currentTimeMillis() - ClientModState.getHandshakeSentAtMs();
        if (elapsed < HANDSHAKE_TIMEOUT_MS) {
            return;
        }
        LOGGER.info("[HellasControl] ClientHandshakeTicker timeout after {} ms", elapsed);
        ClientModState.markHandshakeTimeout("Server requires HellasControl.");
        ClientEnforcer.disconnectForMissingServer("Server requires HellasControl.");
    }
}
