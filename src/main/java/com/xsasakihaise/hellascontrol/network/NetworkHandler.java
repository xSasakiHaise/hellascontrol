package com.xsasakihaise.hellascontrol.network;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseManager;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Owns the SimpleChannel used for the HellasControl ping/pong handshake. The
 * handshake lets clients verify that the connected server both has the mod
 * installed and holds a valid license before gameplay continues.
 */
public final class NetworkHandler {

    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;

    private NetworkHandler(){}

    /** Registers the network channel and both handshake packet types. */
    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(HellasControl.MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        int id = 0;
        CHANNEL.registerMessage(id++, ModPing.class,
                ModPing::encode, ModPing::decode, NetworkHandler::handlePing);

        CHANNEL.registerMessage(id++, ModPong.class,
                ModPong::encode, ModPong::decode, NetworkHandler::handlePong);
    }

    /** Handles {@link ModPing} packets sent from clients once they join a server. */
    private static void handlePing(ModPing msg, Supplier<net.minecraftforge.fml.network.NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            boolean licensed = LicenseEnforcer.isServerLicensed();
            String message = (LicenseManager.getCached() != null) ? LicenseManager.getCached().getMessage() : "";

            // 1.16.5: ServerPlayNetHandler.connection is the NetworkManager
            CHANNEL.sendTo(new ModPong(true, licensed, message),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Handles {@link ModPong} packets on the client after the server responds. */
    private static void handlePong(ModPong msg, Supplier<net.minecraftforge.fml.network.NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            com.xsasakihaise.hellascontrol.ClientModState.onHandshakeResult(
                    msg.hasHellasControl, msg.serverLicensed, msg.message);
            com.xsasakihaise.hellascontrol.client.ClientEnforcer.checkAndDisconnectIfNeeded();
        });
        ctx.get().setPacketHandled(true);
    }
}
