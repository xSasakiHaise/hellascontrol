package com.xsasakihaise.hellascontrol.network;

import com.xsasakihaise.hellascontrol.ClientModState;
import com.xsasakihaise.hellascontrol.HellasControl;
import com.xsasakihaise.hellascontrol.client.ClientEnforcer;
import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseCache;
import com.xsasakihaise.hellascontrol.license.LicenseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the HellasControl handshake lifecycle.
 *
 * <p>NeoForge 1.21 removed the old SimpleChannel API used by previous
 * revisions. Until payload migration is completed, this handler provides a
 * local compatibility path that keeps the client state machine functional
 * during development builds.</p>
 */
public final class NetworkHandler {

    private static final Logger LOGGER = LogManager.getLogger(NetworkHandler.class);

    private NetworkHandler(){}

    /** Registers handshake networking. */
    public static void register() {
        LOGGER.info("[HellasControl] NetworkHandler.register (compat mode)");
    }

    /**
     * Sends a ping request from client logic and immediately resolves the
     * handshake using currently known license data.
     */
    public static void sendPing(int protocolVersion, String modListHash, String sidemodId) {
        LOGGER.info("[HellasControl] NetworkHandler.sendPing protocol={} hash={} sidemod={}",
                protocolVersion, modListHash, sidemodId);

        boolean licensed = LicenseEnforcer.isServerLicensed();
        LicenseCache cache = LicenseManager.getCached();
        String message = (cache != null) ? cache.getMessage() : "";
        String currentToken = cache != null ? cache.getCurrentToken() : "";
        String nextToken = cache != null ? cache.getNextToken() : "";
        long nowMs = System.currentTimeMillis();
        long currentFrom = cache != null && cache.getCurrentValidFrom() != null ? cache.getCurrentValidFrom().toEpochMilli() : 0L;
        long currentTo = cache != null && cache.getCurrentValidTo() != null ? cache.getCurrentValidTo().toEpochMilli() : 0L;
        long nextFrom = cache != null && cache.getNextValidFrom() != null ? cache.getNextValidFrom().toEpochMilli() : 0L;
        long nextTo = cache != null && cache.getNextValidTo() != null ? cache.getNextValidTo().toEpochMilli() : 0L;
        String fingerprint = cache != null ? cache.getServerFingerprint() : "";

        if (HellasControl.debugConfig != null && HellasControl.debugConfig.isDebugHandshake()) {
            LOGGER.info("[HellasControl] Handshake compat response has=true licensed={} message='{}'",
                    licensed, message);
        }

        ClientModState.onHandshakeResult(
                true,
                licensed,
                message,
                currentToken,
                nextToken,
                nowMs,
                currentFrom,
                currentTo,
                nextFrom,
                nextTo,
                fingerprint);
        ClientEnforcer.handleHandshake();
    }
}
