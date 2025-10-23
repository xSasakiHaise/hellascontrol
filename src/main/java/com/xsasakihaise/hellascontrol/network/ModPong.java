package com.xsasakihaise.hellascontrol.network;

import net.minecraft.network.PacketBuffer;

public final class ModPong {
    public final boolean hasHellasControl;
    public final boolean serverLicensed;
    public final String message;

    public ModPong(boolean has, boolean licensed, String msg) {
        this.hasHellasControl = has;
        this.serverLicensed = licensed;
        this.message = msg == null ? "" : msg;
    }

    public static void encode(ModPong m, PacketBuffer buf) {
        buf.writeBoolean(m.hasHellasControl);
        buf.writeBoolean(m.serverLicensed);
        buf.writeUtf(m.message, 32767); // Correct for 1.16.5
    }

    public static ModPong decode(PacketBuffer buf) {
        boolean has = buf.readBoolean();
        boolean lic = buf.readBoolean();
        String msg = buf.readUtf(32767);
        return new ModPong(has, lic, msg);
    }
}
