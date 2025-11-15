package com.xsasakihaise.hellascontrol.network;

import net.minecraft.network.PacketBuffer;

/**
 * Server-to-client packet carrying the results of a license validation run.
 * A successful pong indicates that HellasControl is installed on the server
 * and optionally describes the license state so the client can show a message.
 */
public final class ModPong {
    public final boolean hasHellasControl;
    public final boolean serverLicensed;
    public final String message;

    /**
     * @param has       whether the server confirmed HellasControl is installed
     * @param licensed  whether the connected server validated its license
     * @param msg       optional human-readable status shown to clients
     */
    public ModPong(boolean has, boolean licensed, String msg) {
        this.hasHellasControl = has;
        this.serverLicensed = licensed;
        this.message = msg == null ? "" : msg;
    }

    /** Serializes the pong payload. */
    public static void encode(ModPong m, PacketBuffer buf) {
        buf.writeBoolean(m.hasHellasControl);
        buf.writeBoolean(m.serverLicensed);
        buf.writeUtf(m.message, 32767); // Correct for 1.16.5
    }

    /** Deserializes the pong payload. */
    public static ModPong decode(PacketBuffer buf) {
        boolean has = buf.readBoolean();
        boolean lic = buf.readBoolean();
        String msg = buf.readUtf(32767);
        return new ModPong(has, lic, msg);
    }
}
