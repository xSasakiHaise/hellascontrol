package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Client-side handshake lifecycle helpers driven by the client tick loop. */
public final class ClientHandshake {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandshake.class);

    private ClientHandshake() {}

    /** Called once when the client joins a remote server. */
    public static void onClientLoggedIn() {
        LOGGER.info("[HellasControl] ClientHandshake.onClientLoggedIn");
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        ClientModState.beginHandshake();
        if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
            LOGGER.info("[HellasControl] Sending handshake ping to remote server.");
        }
        NetworkHandler.sendPing(3, buildModListHash(), "");
    }

    /** Called once when the client leaves a remote server. */
    public static void onClientLoggedOut() {
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
