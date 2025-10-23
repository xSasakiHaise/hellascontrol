package com.xsasakihaise.hellascontrol;

public final class ClientModState {
    private static volatile boolean serverHasHellas = false;
    private static volatile boolean serverLicensed = false;
    private static volatile String  serverMessage   = "";

    private ClientModState() {}

    public static void onHandshakeResult(boolean hasHellas, boolean isLicensed, String message) {
        serverHasHellas = hasHellas;
        serverLicensed  = isLicensed;
        serverMessage   = message != null ? message : "";
    }

    public static boolean isServerLicensed() { return serverHasHellas && serverLicensed; }
    public static String  getServerMessage() { return serverMessage; }
}
