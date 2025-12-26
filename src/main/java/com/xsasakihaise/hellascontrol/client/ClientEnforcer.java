package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler; // MCP/SRG
import net.minecraft.util.text.StringTextComponent;            // MCP/SRG

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
    public static void checkAndDisconnectIfNeeded() {
        LOGGER.info("[HellasControl] ClientEnforcer.checkAndDisconnectIfNeeded");
        Minecraft mc = Minecraft.getInstance();
        if (!ClientConnectionUtil.isRemoteConnection(mc)) {
            return;
        }
        if (!ClientModState.isServerLicensed()) {
            String msg = ClientModState.getServerMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "Server is not licensed. Please contact the server owner.";
            }
            String finalMsg = msg;

            ClientPlayNetHandler handler = mc.getConnection();
            if (handler != null) {
                // This shows the “Disconnected” screen with your message and closes the connection.
                LOGGER.info("[HellasControl] ClientEnforcer.disconnect (unlicensed) message='{}'", finalMsg);
                mc.execute(() -> handler.onDisconnect(new StringTextComponent(finalMsg)));
            }
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
        ClientPlayNetHandler handler = mc.getConnection();
        if (handler != null) {
            LOGGER.info("[HellasControl] ClientEnforcer.disconnect (missing server) message='{}'", msg);
            mc.execute(() -> handler.onDisconnect(new StringTextComponent(msg)));
        }
    }
}
