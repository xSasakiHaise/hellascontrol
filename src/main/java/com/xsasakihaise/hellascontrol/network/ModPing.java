package com.xsasakihaise.hellascontrol.network;

import net.minecraft.network.PacketBuffer;

/**
 * Empty client-to-server packet used purely as a trigger for the licensing
 * handshake. The server responds with {@link ModPong}.
 */
public final class ModPing {
    public ModPing() {}

    /** Packet carries no payload. */
    public static void encode(ModPing m, PacketBuffer buf) {}

    /** Packet carries no payload. */
    public static ModPing decode(PacketBuffer buf) { return new ModPing(); }
}
