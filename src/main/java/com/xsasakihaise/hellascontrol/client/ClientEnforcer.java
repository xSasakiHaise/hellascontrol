package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

/**
 * Disconnects the player if the connected server is not running a valid
 * HellasControl installation.
 */
public final class ClientEnforcer {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientEnforcer.class);
    private ClientEnforcer() {}

    /**
     * Checks the last handshake and forces a disconnect with a descriptive
     * message if the server is missing or unlicensed.
     */
    public static void handleHandshake() {
        LOGGER.info("[HellasControl] ClientEnforcer.handleHandshake");
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        if (!ClientModState.hasHandshakeResponse()) {
            return;
        }
        if (!ClientModState.hasServerHellas()) {
            disconnectForMissingServer("Server requires HellasControl.");
            return;
        }
        if (!ClientModState.isServerReportedLicensed()) {
            String msg = ClientModState.getServerMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "Server is not licensed. Please contact the server owner.";
            }
            disconnectForInvalidToken(msg);
            return;
        }
        if (!ClientModState.isServerLicensed() && !ClientModState.isTokenValidationComplete()) {
            if (!ClientModState.isTokenValidationInFlight()) {
                validateServerToken();
            }
        }
        if (!ClientModState.isServerLicensed() && ClientModState.isTokenValidationComplete()) {
            disconnectForInvalidToken("This server is not licensed to run HellasControl / Hephaestus Forge software.");
        }
    }

    public static void disconnectForMissingServer(String message) {
        LOGGER.info("[HellasControl] ClientEnforcer.disconnectForMissingServer message='{}'", message);
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        String msg = (message == null || message.isEmpty())
                ? "Server requires HellasControl."
                : message;
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) {
            LOGGER.info("[HellasControl] ClientEnforcer.disconnect (missing server) message='{}'", msg);
            mc.execute(() -> handler.onDisconnect(Component.literal(msg)));
        }
    }

    public static void disconnectForInvalidToken(String message) {
        LOGGER.info("[HellasControl] ClientEnforcer.disconnectForInvalidToken message='{}'", message);
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        String msg = (message == null || message.isEmpty())
                ? "This server is not licensed to run HellasControl / Hephaestus Forge software."
                : message;
        ClientPacketListener handler = mc.getConnection();
        if (handler != null) {
            mc.execute(() -> handler.onDisconnect(Component.literal(msg)));
        }
    }

    private static void validateServerToken() {
        if (!ClientModState.isHandshakePending() && ClientModState.hasHandshakeResponse()) {
            ClientTokenVerifier.verifyTokenAsync();
        }
    }
}
