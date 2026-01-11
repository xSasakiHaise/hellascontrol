package com.xsasakihaise.hellascontrol.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Empty client-to-server packet used purely as a trigger for the licensing
 * handshake. The server responds with {@link ModPong}.
 */
public final class ModPing {
    private final int protocolVersion;
    private final String modListHash;
    private final String sidemodId;

    public ModPing(int protocolVersion, String modListHash, String sidemodId) {
        this.protocolVersion = protocolVersion;
        this.modListHash = modListHash == null ? "" : modListHash;
        this.sidemodId = sidemodId == null ? "" : sidemodId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getModListHash() {
        return modListHash;
    }

    public String getSidemodId() {
        return sidemodId;
    }

    /** Packet carries the handshake version and optional mod list hash. */
    public static void encode(ModPing m, FriendlyByteBuf buf) {
        buf.writeVarInt(m.protocolVersion);
        buf.writeUtf(m.modListHash, 32767);
        buf.writeUtf(m.sidemodId, 32767);
    }

    /** Packet carries the handshake version and optional mod list hash. */
    public static ModPing decode(FriendlyByteBuf buf) {
        int version = buf.readVarInt();
        String hash = buf.readUtf(32767);
        String sidemod = buf.readUtf(32767);
        return new ModPing(version, hash, sidemod);
    }
}
