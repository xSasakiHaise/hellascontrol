package com.xsasakihaise.hellascontrol.enforcement;

import com.xsasakihaise.hellascontrol.license.LicenseCache;
import com.xsasakihaise.hellascontrol.license.LicenseManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

/**
 * Performs the actual license verification and exposes the result for other
 * systems such as the network handshake.
 */
public final class LicenseEnforcer {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(LicenseEnforcer.class);
    private static volatile boolean serverLicensed = false;

    private LicenseEnforcer() {}

    /**
     * Runs the {@link LicenseManager} verification and remembers the result.
     *
     * @return {@code true} if the license is valid according to the last check
     */
    public static boolean enforceServerLicense() {
        LOGGER.info("[HellasControl] LicenseEnforcer.enforceServerLicense");
        serverLicensed = LicenseManager.verifyServer();
        System.out.println("[HellasControl] Server license status: " + (serverLicensed ? "VALID" : "INVALID"));
        return serverLicensed;
    }

    public static boolean enforceEntitlements(MinecraftServer server) {
        if (server == null || !server.isDedicatedServer()) {
            return true;
        }
        LicenseCache cache = LicenseManager.getCached();
        if (cache == null || !cache.isLicensed()) {
            LOGGER.error("[HellasControl] Entitlement check failed: license invalid.");
            return false;
        }
        java.util.Set<String> entitled = new java.util.HashSet<>();
        for (String ent : cache.getEntitlements()) {
            if (ent != null && !ent.isBlank()) {
                entitled.add(ent.toLowerCase(java.util.Locale.ROOT));
            }
        }
        java.util.List<String> unauthorized = new java.util.ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modId = mod.getModId();
            if (modId.startsWith("hellas") && !modId.equals("hellascontrol")) {
                String key = modId.replace("hellas", "");
                if (!entitled.contains(key.toLowerCase(java.util.Locale.ROOT))) {
                    unauthorized.add(modId);
                }
            }
        }
        if (!unauthorized.isEmpty()) {
            LOGGER.error("[HellasControl] Unlicensed Hellas mods detected: {}", unauthorized);
            return false;
        }
        return true;
    }

    public static void startExpiredTokenAnnouncements(MinecraftServer server) {
        if (server == null || !server.isDedicatedServer()) {
            return;
        }
        java.util.concurrent.ScheduledExecutorService executor =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "hellascontrol-token-expiry");
                    t.setDaemon(true);
                    return t;
                });
        executor.scheduleAtFixedRate(() -> {
            try {
                LicenseCache cache = LicenseManager.getCached();
                if (cache == null) {
                    return;
                }
                if (cache.isExpiredWithoutNext(java.time.Instant.now())) {
                    server.execute(() -> server.getPlayerList()
                            .broadcastSystemMessage(Component.literal(
                                    "validation hash expired, pls restart the server to get a new one"), false));
                }
            } catch (Exception e) {
                LOGGER.debug("[HellasControl] Token expiry announcement failed: {}", e.getMessage());
            }
        }, 60, 60, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * @return the most recent outcome of {@link #enforceServerLicense()}
     */
    public static boolean isServerLicensed() {
        return serverLicensed;
    }
}
