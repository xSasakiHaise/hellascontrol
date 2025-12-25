package com.xsasakihaise.hellascontrol.network;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final String PROTOCOL = "2";
    private static final Logger LOGGER = LogManager.getLogger(NetworkHandler.class);
    public static SimpleChannel CHANNEL;

    private NetworkHandler(){}

    /** Registers the network channel and both handshake packet types. */
    public static void register() {
        LOGGER.info("[HellasControl] NetworkHandler.register");
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(HellasControl.MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        int id = 0;
        int pingId = id++;
        CHANNEL.registerMessage(pingId, ModPing.class,
                ModPing::encode, ModPing::decode, NetworkHandler::handlePing);

        int pongId = id++;
        CHANNEL.registerMessage(pongId, ModPong.class,
                ModPong::encode, ModPong::decode, NetworkHandler::handlePong);

        if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugPacketRegistry()) {
            LOGGER.info("[HellasControl] Packet registry map: {} -> {}, {} -> {}",
                    pingId, ModPing.class.getName(), pongId, ModPong.class.getName());
        }
    }

    /** Handles {@link ModPing} packets sent from clients once they join a server. */
    private static void handlePing(ModPing msg, Supplier<net.minecraftforge.fml.network.NetworkEvent.Context> ctx) {
        LOGGER.info("[HellasControl] NetworkHandler.handlePing");
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            boolean licensed = LicenseEnforcer.isServerLicensed();
            String message = (LicenseManager.getCached() != null) ? LicenseManager.getCached().getMessage() : "";
            ModPong.HandshakeReason reason = licensed ? ModPong.HandshakeReason.OK : ModPong.HandshakeReason.UNLICENSED;
            if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
                LOGGER.info("[HellasControl] Handshake ping from {} -> has=true licensed={} message='{}'",
                        player != null ? player.getGameProfile().getName() : "<unknown>", licensed, message);
            }

            // 1.16.5: ServerPlayNetHandler.connection is the NetworkManager
            CHANNEL.sendTo(new ModPong(true, licensed, reason, message),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Handles {@link ModPong} packets on the client after the server responds. */
    private static void handlePong(ModPong msg, Supplier<net.minecraftforge.fml.network.NetworkEvent.Context> ctx) {
        LOGGER.info("[HellasControl] NetworkHandler.handlePong");
        ctx.get().enqueueWork(() -> {
            if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
                LOGGER.info("[HellasControl] Handshake ack received: has={} licensed={} reason={} message='{}'",
                        msg.hasHellasControl, msg.serverLicensed, msg.reason, msg.message);
            }
            com.xsasakihaise.hellascontrol.ClientModState.onHandshakeResult(
                    msg.hasHellasControl, msg.serverLicensed, msg.message);
            com.xsasakihaise.hellascontrol.client.ClientEnforcer.checkAndDisconnectIfNeeded();
        });
        ctx.get().setPacketHandled(true);
    }
}
