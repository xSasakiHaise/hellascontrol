package com.xsasakihaise.hellascontrol.network;

import net.minecraft.network.PacketBuffer;

public final class ModPing {
    public ModPing() {}

    public static void encode(ModPing m, PacketBuffer buf) {}
    public static ModPing decode(PacketBuffer buf) { return new ModPing(); }
}
