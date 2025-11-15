package com.xsasakihaise.hellascontrol;

/**
 * Thread-safe holder for the last handshake state received from the server.
 * The information is queried by various client-only hooks to decide whether
 * the remote server is licensed and to display custom disconnect reasons.
 */
public final class ClientModState {
    private static volatile boolean serverHasHellas = false;
    private static volatile boolean serverLicensed = false;
    private static volatile String  serverMessage   = "";

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
        serverHasHellas = hasHellas;
        serverLicensed  = isLicensed;
        serverMessage   = message != null ? message : "";
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
}
