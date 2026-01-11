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
    private static volatile String serverToken = "";
    private static volatile String serverTokenNext = "";
    private static volatile long serverTimeEpochMillis = 0L;
    private static volatile long tokenValidFromEpochMillis = 0L;
    private static volatile long tokenValidToEpochMillis = 0L;
    private static volatile long nextTokenValidFromEpochMillis = 0L;
    private static volatile long nextTokenValidToEpochMillis = 0L;
    private static volatile String serverFingerprint = "";
    private static volatile boolean handshakePending = false;
    private static volatile boolean handshakeReceived = false;
    private static volatile long handshakeSentAtMs = 0L;
    private static volatile boolean tokenValidationInFlight = false;
    private static volatile boolean tokenValidationComplete = false;
    private static volatile boolean tokenValid = false;

    private ClientModState() {}

    /**
     * Called once a {@link com.xsasakihaise.hellascontrol.network.ModPong}
     * packet arrives from the server.
     *
     * @param hasHellas   whether the server confirmed HellasControl is installed
     * @param isLicensed  whether the server's license is valid
     * @param message     optional human-readable status text supplied by the server
     */
    public static void onHandshakeResult(boolean hasHellas, boolean isLicensed, String message,
                                         String token, String nextToken,
                                         long serverTimeMs, long validFromMs, long validToMs,
                                         long nextValidFromMs, long nextValidToMs,
                                         String fingerprint) {
        LOGGER.info("[HellasControl] ClientModState.onHandshakeResult hasHellas={} licensed={} message='{}'",
                hasHellas, isLicensed, message);
        serverHasHellas = hasHellas;
        serverLicensed  = isLicensed;
        serverMessage   = message != null ? message : "";
        serverToken = token != null ? token : "";
        serverTokenNext = nextToken != null ? nextToken : "";
        serverTimeEpochMillis = serverTimeMs;
        tokenValidFromEpochMillis = validFromMs;
        tokenValidToEpochMillis = validToMs;
        nextTokenValidFromEpochMillis = nextValidFromMs;
        nextTokenValidToEpochMillis = nextValidToMs;
        serverFingerprint = fingerprint != null ? fingerprint : "";
        handshakePending = false;
        handshakeReceived = true;
        tokenValidationInFlight = false;
        tokenValidationComplete = false;
        tokenValid = false;
    }

    /**
     * @return {@code true} only when the connected server both runs
     * HellasControl and reports a valid license.
     */
    public static boolean isServerLicensed() { return serverHasHellas && serverLicensed && tokenValid; }

    public static boolean isServerReportedLicensed() { return serverHasHellas && serverLicensed; }

    public static boolean hasServerHellas() { return serverHasHellas; }

    /**
     * @return descriptive message accompanying the last handshake response
     */
    public static String  getServerMessage() { return serverMessage; }

    public static String getServerToken() { return serverToken; }

    public static String getServerTokenNext() { return serverTokenNext; }

    public static long getServerTimeEpochMillis() { return serverTimeEpochMillis; }

    public static long getTokenValidFromEpochMillis() { return tokenValidFromEpochMillis; }

    public static long getTokenValidToEpochMillis() { return tokenValidToEpochMillis; }

    public static long getNextTokenValidFromEpochMillis() { return nextTokenValidFromEpochMillis; }

    public static long getNextTokenValidToEpochMillis() { return nextTokenValidToEpochMillis; }

    public static String getServerFingerprint() { return serverFingerprint; }

    public static void beginHandshake() {
        LOGGER.info("[HellasControl] ClientModState.beginHandshake");
        serverHasHellas = false;
        serverLicensed = false;
        serverMessage = "";
        serverToken = "";
        serverTokenNext = "";
        serverTimeEpochMillis = 0L;
        tokenValidFromEpochMillis = 0L;
        tokenValidToEpochMillis = 0L;
        nextTokenValidFromEpochMillis = 0L;
        nextTokenValidToEpochMillis = 0L;
        serverFingerprint = "";
        handshakePending = true;
        handshakeReceived = false;
        handshakeSentAtMs = System.currentTimeMillis();
        tokenValidationInFlight = false;
        tokenValidationComplete = false;
        tokenValid = false;
    }

    public static void markHandshakeTimeout(String message) {
        LOGGER.info("[HellasControl] ClientModState.markHandshakeTimeout message='{}'", message);
        serverHasHellas = false;
        serverLicensed = false;
        serverMessage = message != null ? message : "";
        handshakePending = false;
        handshakeReceived = false;
        tokenValidationInFlight = false;
        tokenValidationComplete = false;
        tokenValid = false;
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
        serverToken = "";
        serverTokenNext = "";
        serverTimeEpochMillis = 0L;
        tokenValidFromEpochMillis = 0L;
        tokenValidToEpochMillis = 0L;
        nextTokenValidFromEpochMillis = 0L;
        nextTokenValidToEpochMillis = 0L;
        serverFingerprint = "";
        handshakePending = false;
        handshakeReceived = false;
        handshakeSentAtMs = 0L;
        tokenValidationInFlight = false;
        tokenValidationComplete = false;
        tokenValid = false;
    }

    public static boolean isTokenValidationInFlight() {
        return tokenValidationInFlight;
    }

    public static boolean isTokenValidationComplete() {
        return tokenValidationComplete;
    }

    public static boolean isTokenValid() {
        return tokenValid;
    }

    public static void markTokenValidationInFlight() {
        tokenValidationInFlight = true;
    }

    public static void markTokenValidationResult(boolean valid) {
        tokenValidationInFlight = false;
        tokenValidationComplete = true;
        tokenValid = valid;
    }
}
