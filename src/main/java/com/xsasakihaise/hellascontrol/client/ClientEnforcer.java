// src/main/java/com/xsasakihaise/hellascontrol/client/ClientEnforcer.java
package com.xsasakihaise.hellascontrol.client;

import com.xsasakihaise.hellascontrol.ClientModState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler; // MCP/SRG
import net.minecraft.util.text.StringTextComponent;            // MCP/SRG

public final class ClientEnforcer {
    private ClientEnforcer() {}

    public static void checkAndDisconnectIfNeeded() {
        if (!ClientModState.isServerLicensed()) {
            String msg = ClientModState.getServerMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "HellasControl: The server is missing a valid license. Connection blocked.";
            }

            Minecraft mc = Minecraft.getInstance();
            ClientPlayNetHandler handler = mc.getConnection();
            if (handler != null) {
                // This shows the “Disconnected” screen with your message and closes the connection.
                handler.onDisconnect(new StringTextComponent(msg));
            }
        }
    }
}
