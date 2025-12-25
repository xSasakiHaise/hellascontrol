package com.xsasakihaise.hellascontrol;

/**
 * Thread-safe holder for the last handshake state received from the server.
 * The information is queried by various client-only hooks to decide whether
 * the remote server is licensed and to display custom disconnect reasons.
 */
public final class ClientModState {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(ClientModState.class);
    private static volatile boolean serverHasHellas = false;
    private static volatile boolean serverLicensed = false;
    private static volatile String  serverMessage   = "";
    private static volatile boolean handshakePending = false;
    private static volatile boolean handshakeReceived = false;
    private static volatile long handshakeSentAtMs = 0L;

    private ClientModState() {}

    /**
     * Called once a {@link com.xsasakihaise.hellascontrol.network.ModPong}
     * packet arrives from the server.
     *
     * @param hasHellas   whether the server confirmed HellasControl is installed
     * @param isLicensed  whether the server's license is valid
     * @param message     optional human-readable status text supplied by the server
     */
    public static void onHandshakeResult(boolean hasHellas, boolean isLicensed, String message) {
        LOGGER.info("[HellasControl] ClientModState.onHandshakeResult hasHellas={} licensed={} message='{}'",
                hasHellas, isLicensed, message);
        serverHasHellas = hasHellas;
        serverLicensed  = isLicensed;
        serverMessage   = message != null ? message : "";
        handshakePending = false;
        handshakeReceived = true;
    }

    /**
     * @return {@code true} only when the connected server both runs
     * HellasControl and reports a valid license.
     */
    public static boolean isServerLicensed() { return serverHasHellas && serverLicensed; }

    /**
     * @return descriptive message accompanying the last handshake response
     */
    public static String  getServerMessage() { return serverMessage; }

    public static void beginHandshake() {
        LOGGER.info("[HellasControl] ClientModState.beginHandshake");
        serverHasHellas = false;
        serverLicensed = false;
        serverMessage = "";
        handshakePending = true;
        handshakeReceived = false;
        handshakeSentAtMs = System.currentTimeMillis();
    }

    public static void markHandshakeTimeout(String message) {
        LOGGER.info("[HellasControl] ClientModState.markHandshakeTimeout message='{}'", message);
        serverHasHellas = false;
        serverLicensed = false;
        serverMessage = message != null ? message : "";
        handshakePending = false;
        handshakeReceived = false;
    }

    public static boolean isHandshakePending() {
        return handshakePending;
    }

    public static boolean hasHandshakeResponse() {
        return handshakeReceived;
    }

    public static long getHandshakeSentAtMs() {
        return handshakeSentAtMs;
    }

    public static void clear() {
        LOGGER.info("[HellasControl] ClientModState.clear");
        serverHasHellas = false;
        serverLicensed = false;
        serverMessage = "";
        handshakePending = false;
        handshakeReceived = false;
        handshakeSentAtMs = 0L;
    }
}
