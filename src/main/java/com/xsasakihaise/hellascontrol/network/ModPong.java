package com.xsasakihaise.hellascontrol.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server-to-client packet carrying the results of a license validation run.
 * A successful pong indicates that HellasControl is installed on the server
 * and optionally describes the license state so the client can show a message.
 */
public final class ModPong {
    private static final Logger LOGGER = LogManager.getLogger(ModPong.class);
    public final boolean hasHellasControl;
    public final boolean serverLicensed;
    public final String message;
    public final HandshakeReason reason;
    public final String serverToken;
    public final String serverTokenNext;
    public final long serverTimeEpochMillis;
    public final long tokenValidFromEpochMillis;
    public final long tokenValidToEpochMillis;
    public final long nextTokenValidFromEpochMillis;
    public final long nextTokenValidToEpochMillis;
    public final String serverFingerprint;

    /**
     * @param has       whether the server confirmed HellasControl is installed
     * @param licensed  whether the connected server validated its license
     * @param reason    machine-readable reason code
     * @param msg       optional human-readable status shown to clients
     */
    public ModPong(boolean has, boolean licensed, HandshakeReason reason, String msg,
                   String serverToken, String serverTokenNext,
                   long serverTimeEpochMillis, long tokenValidFromEpochMillis, long tokenValidToEpochMillis,
                   long nextTokenValidFromEpochMillis, long nextTokenValidToEpochMillis,
                   String serverFingerprint) {
        this.hasHellasControl = has;
        this.serverLicensed = licensed;
        this.reason = reason == null ? HandshakeReason.OK : reason;
        this.message = msg == null ? "" : msg;
        this.serverToken = serverToken == null ? "" : serverToken;
        this.serverTokenNext = serverTokenNext == null ? "" : serverTokenNext;
        this.serverTimeEpochMillis = serverTimeEpochMillis;
        this.tokenValidFromEpochMillis = tokenValidFromEpochMillis;
        this.tokenValidToEpochMillis = tokenValidToEpochMillis;
        this.nextTokenValidFromEpochMillis = nextTokenValidFromEpochMillis;
        this.nextTokenValidToEpochMillis = nextTokenValidToEpochMillis;
        this.serverFingerprint = serverFingerprint == null ? "" : serverFingerprint;
    }

    /** Serializes the pong payload. */
    public static void encode(ModPong m, FriendlyByteBuf buf) {
        LOGGER.info("[HellasControl] ModPong.encode has={} licensed={} reason={} message='{}'",
                m.hasHellasControl, m.serverLicensed, m.reason, m.message);
        buf.writeBoolean(m.hasHellasControl);
        buf.writeBoolean(m.serverLicensed);
        buf.writeVarInt(m.reason.getId());
        buf.writeUtf(m.message, 32767); // Correct for 1.21.1
        buf.writeUtf(m.serverToken, 32767);
        buf.writeUtf(m.serverTokenNext, 32767);
        buf.writeLong(m.serverTimeEpochMillis);
        buf.writeLong(m.tokenValidFromEpochMillis);
        buf.writeLong(m.tokenValidToEpochMillis);
        buf.writeLong(m.nextTokenValidFromEpochMillis);
        buf.writeLong(m.nextTokenValidToEpochMillis);
        buf.writeUtf(m.serverFingerprint, 32767);
    }

    /** Deserializes the pong payload. */
    public static ModPong decode(FriendlyByteBuf buf) {
        boolean has = buf.readBoolean();
        boolean lic = buf.readBoolean();
        int reasonId = buf.readVarInt();
        HandshakeReason reason = HandshakeReason.fromId(reasonId);
        if (reason == HandshakeReason.UNKNOWN) {
            LOGGER.warn("[HellasControl] Handshake reason out of range: {}", reasonId);
        }
        String msg = buf.readUtf(32767);
        String token = buf.readUtf(32767);
        String nextToken = buf.readUtf(32767);
        long serverTimeMs = buf.readLong();
        long validFromMs = buf.readLong();
        long validToMs = buf.readLong();
        long nextValidFromMs = buf.readLong();
        long nextValidToMs = buf.readLong();
        String fingerprint = buf.readUtf(32767);
        LOGGER.info("[HellasControl] ModPong.decode has={} licensed={} reason={} message='{}'",
                has, lic, reason, msg);
        return new ModPong(has, lic, reason, msg, token, nextToken, serverTimeMs, validFromMs, validToMs,
                nextValidFromMs, nextValidToMs, fingerprint);
    }

    public enum HandshakeReason {
        OK(0),
        UNLICENSED(1),
        UNKNOWN(2);

        private final int id;

        HandshakeReason(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static HandshakeReason fromId(int id) {
            for (HandshakeReason value : values()) {
                if (value.id == id) {
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}
