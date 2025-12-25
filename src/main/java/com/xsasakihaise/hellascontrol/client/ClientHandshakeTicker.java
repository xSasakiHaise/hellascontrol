package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientHandshakeTicker {
    private static final long HANDSHAKE_TIMEOUT_MS = 2500L;
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientHandshakeTicker.class);

    private ClientHandshakeTicker() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!ClientModState.isHandshakePending()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
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
