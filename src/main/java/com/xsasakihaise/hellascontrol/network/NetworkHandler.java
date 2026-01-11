package com.xsasakihaise.hellascontrol.network;

import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseCache;
import com.xsasakihaise.hellascontrol.license.LicenseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Owns the SimpleChannel used for the HellasControl ping/pong handshake. The
 * handshake lets clients verify that the connected server both has the mod
 * installed and holds a valid license before gameplay continues.
 */
public final class NetworkHandler {

    private static final String PROTOCOL = "3";
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
    private static void handlePing(ModPing msg, Supplier<net.neoforged.neoforge.network.NetworkEvent.Context> ctx) {
        LOGGER.info("[HellasControl] NetworkHandler.handlePing");
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            boolean licensed = LicenseEnforcer.isServerLicensed();
            LicenseCache cache = LicenseManager.getCached();
            String message = (cache != null) ? cache.getMessage() : "";
            ModPong.HandshakeReason reason = licensed ? ModPong.HandshakeReason.OK : ModPong.HandshakeReason.UNLICENSED;
            if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
                LOGGER.info("[HellasControl] Handshake ping from {} -> has=true licensed={} message='{}'",
                        player != null ? player.getGameProfile().getName() : "<unknown>", licensed, message);
            }
            String currentToken = cache != null ? cache.getCurrentToken() : "";
            String nextToken = cache != null ? cache.getNextToken() : "";
            long nowMs = System.currentTimeMillis();
            long currentFrom = cache != null && cache.getCurrentValidFrom() != null ? cache.getCurrentValidFrom().toEpochMilli() : 0L;
            long currentTo = cache != null && cache.getCurrentValidTo() != null ? cache.getCurrentValidTo().toEpochMilli() : 0L;
            long nextFrom = cache != null && cache.getNextValidFrom() != null ? cache.getNextValidFrom().toEpochMilli() : 0L;
            long nextTo = cache != null && cache.getNextValidTo() != null ? cache.getNextValidTo().toEpochMilli() : 0L;
            String fingerprint = cache != null ? cache.getServerFingerprint() : "";

            CHANNEL.sendTo(new ModPong(true, licensed, reason, message, currentToken, nextToken,
                            nowMs, currentFrom, currentTo, nextFrom, nextTo, fingerprint),
                    player.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }

    /** Handles {@link ModPong} packets on the client after the server responds. */
    private static void handlePong(ModPong msg, Supplier<net.neoforged.neoforge.network.NetworkEvent.Context> ctx) {
        LOGGER.info("[HellasControl] NetworkHandler.handlePong");
        ctx.get().enqueueWork(() -> {
            if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
                LOGGER.info("[HellasControl] Handshake ack received: has={} licensed={} reason={} message='{}'",
                        msg.hasHellasControl, msg.serverLicensed, msg.reason, msg.message);
            }
            com.xsasakihaise.hellascontrol.ClientModState.onHandshakeResult(
                    msg.hasHellasControl, msg.serverLicensed, msg.message,
                    msg.serverToken, msg.serverTokenNext,
                    msg.serverTimeEpochMillis, msg.tokenValidFromEpochMillis, msg.tokenValidToEpochMillis,
                    msg.nextTokenValidFromEpochMillis, msg.nextTokenValidToEpochMillis,
                    msg.serverFingerprint);
            com.xsasakihaise.hellascontrol.client.ClientEnforcer.handleHandshake();
        });
        ctx.get().setPacketHandled(true);
    }
}
