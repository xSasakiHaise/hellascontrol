package com.xsasakihaise.hellascontrol;

import com.xsasakihaise.hellascontrol.commands.HellasControlDependenciesCommand;
import com.xsasakihaise.hellascontrol.commands.HellasControlFeaturesCommand;
import com.xsasakihaise.hellascontrol.commands.HellasControlVersionCommand;
import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseCache;
import com.xsasakihaise.hellascontrol.license.LicenseManager;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Locale;

/**
 * HellasControl — core mod that:
 *  - loads default config/info
 *  - initializes server-side license cache
 *  - performs server license enforcement
 *  - exposes ping/pong handshake for clients
 *  - registers debug/admin commands
 *
 * Clients do NOT hold a license; they only verify that the connected server
 * runs HellasControl and is licensed (via network handshake).
 */
@Mod(HellasControl.MODID)
public class HellasControl {

    public static final String MODID = "hellascontrol";

    public static HellasControlInfoConfig infoConfig;
    private static volatile boolean initialized = false;

    public HellasControl() {
        // Load default display/info config bundled in the jar
        infoConfig = new HellasControlInfoConfig();
        infoConfig.loadDefaultsFromResource();

        // Register MOD-bus listeners (lifecycle)
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        // Register FORGE-bus listeners (server start, commands)
        MinecraftForge.EVENT_BUS.register(this);

        // Network channel for client<->server handshake (ping/pong)
        NetworkHandler.register();
    }

    /**
     * Runs during mod common setup (safe place for queued work).
     */
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> initialized = true);
    }

    /**
     * SERVER-ONLY: fire when a dedicated server starts.
     * Initializes license manager (reads config/hellas/license.json) and enforces license.
     */
    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        // Resolve the dedicated server's root directory -> .../config/hellas/license.json
        java.nio.file.Path serverRoot = event.getServer().getServerDirectory().toPath();

        // Initialize license cache (local json for now; remote-ready later)
        LicenseManager.initialize(serverRoot);

        // Enforce server license (sets internal flag used by handshake)
        boolean ok = LicenseEnforcer.enforceServerLicense();
        if (!ok) {
            System.err.println("[HellasControl] WARNING: Server not licensed. Clients will refuse to play.");
        }

        // Load/refresh human-readable info config from server root, if you keep it there
        // (optional; no-op if your HellasControlInfoConfig handles only in-jar defaults)
        // infoConfig.load(serverRoot.toFile());
    }

    /**
     * Register admin/debug commands.
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        HellasControlVersionCommand.register(event.getDispatcher());
        HellasControlDependenciesCommand.register(event.getDispatcher());
        HellasControlFeaturesCommand.register(event.getDispatcher());
    }

    /**
     * For general availability checks.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Client convenience (filled via network pong).
     */
    public static boolean isServerLicensedClientSide() {
        return com.xsasakihaise.hellascontrol.ClientModState.isServerLicensed();
    }

    /**
     * SERVER-SIDE: does the license include a given entitlement?
     */
    public static boolean hasEntitlement(String key) {
        if (key == null) return false;

        LicenseCache cache = LicenseManager.getCached();
        if (cache == null || !cache.isValid()) return false;

        String norm = key.toLowerCase(Locale.ROOT);
        java.util.List<String> ents = cache.getEntitlements();
        if (ents == null || ents.isEmpty()) return false;

        for (String s : ents) {
            if (s != null && norm.equals(s.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * SERVER-SIDE helper for sidemods: throw if entitlement missing (fail fast on startup).
     */
    public static void requireEntitlement(String key) {
        if (!hasEntitlement(key)) {
            throw new IllegalStateException("HellasControl: Missing entitlement '" + key + "'.");
        }
    }
}