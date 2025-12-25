package com.xsasakihaise.hellascontrol.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;

final class ClientConnectionUtil {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientConnectionUtil.class);
    private ClientConnectionUtil() {}

    static boolean isRemoteConnection(Minecraft mc) {
        if (mc == null || mc.getSingleplayerServer() != null) {
            LOGGER.info("[HellasControl] ClientConnectionUtil.isRemoteConnection -> false (singleplayer or null)");
            return false;
        }
        ClientPlayNetHandler handler = mc.getConnection();
        if (handler == null || handler.getConnection() == null) {
            LOGGER.info("[HellasControl] ClientConnectionUtil.isRemoteConnection -> false (no connection)");
            return false;
        }
        boolean remote = !handler.getConnection().isMemoryConnection();
        LOGGER.info("[HellasControl] ClientConnectionUtil.isRemoteConnection -> {}", remote);
        return remote;
    }
}
