package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.network.ModPing;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import com.xsasakihaise.hellascontrol.ClientModState;
import com.xsasakihaise.hellascontrol.HellasControl;
import net.neoforged.fml.ModList;

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
        NetworkHandler.CHANNEL.sendToServer(new ModPing(3, buildModListHash(), ""));
    }

    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        LOGGER.info("[HellasControl] ClientHandshake.onClientLoggedOut");
        ClientModState.clear();
    }

    private static String buildModListHash() {
        try {
            java.util.List<String> ids = ModList.get().getMods().stream()
                    .map(mod -> mod.getModId())
                    .sorted()
                    .toList();
            String joined = String.join(",", ids);
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(joined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
