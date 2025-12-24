package com.xsasakihaise.hellascontrol;

import com.xsasakihaise.hellascontrol.enforcement.LicenseEnforcer;
import com.xsasakihaise.hellascontrol.license.LicenseCache;
import com.xsasakihaise.hellascontrol.license.LicenseManager;
import com.xsasakihaise.hellascontrol.network.NetworkHandler;
import com.xsasakihaise.hellascontrol.bisect.AutoBisectRunner;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.ModList;
import net.minecraft.world.server.ServerWorld;

import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Entry point for the Hellas Control core mod.
 * <p>
 * The class wires the lifecycle listeners used to bootstrap licensing
 * infrastructure on the dedicated server and to keep clients informed about the
 * state of that license via a lightweight ping/pong handshake. Besides license
 * handling, it also loads an informational config bundled with the mod that can
 * be surfaced by other Hellas sidemods.
 * </p>
 * <p>
 * Clients do not store or own licenses; they only query the connected server
 * for proof that HellasControl is present and properly licensed.
 * </p>
 */
@Mod(HellasControl.MODID)
public class HellasControl {

    public static final String MODID = "hellascontrol";
    private static final Logger LOGGER = LogManager.getLogger(HellasControl.class);
    private static final Marker DIAGNOSTICS = MarkerManager.getMarker("HELLASCONTROL");

    public static HellasControlInfoConfig infoConfig;
    private static volatile boolean initialized = false;

    /**
     * Constructs the mod instance and registers lifecycle listeners for both
     * the mod event bus and the global Forge bus. All heavy work is deferred to
     * explicit callbacks so that Forge can control the threading model.
     */
    public HellasControl() {
        // Load default display/info config bundled in the jar
        infoConfig = new HellasControlInfoConfig();
        infoConfig.loadDefaultsFromResource();

        // Register MOD-bus listeners (lifecycle)
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        // Register FORGE-bus listeners (server start)
        MinecraftForge.EVENT_BUS.register(this);

        // Network channel for client<->server handshake (ping/pong)
        NetworkHandler.register();

        logModList();
    }

    /**
     * Runs during the mod's common setup stage. This is the recommended stage
     * for non-world-thread work queues in Forge 1.16.x and is where we flip the
     * {@link #initialized} flag once the mod is ready to service API calls.
     */
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info(DIAGNOSTICS, "[{}] CommonSetup start", MODID);
        event.enqueueWork(() -> {
            initialized = true;
            LOGGER.info(DIAGNOSTICS, "[{}] CommonSetup end", MODID);
        });
    }

    /**
     * SERVER-ONLY callback fired when a dedicated server finishes its startup
     * sequence. The method initializes the {@link LicenseManager} to read the
     * server's {@code config/hellas/license.json} file and immediately runs the
     * {@link LicenseEnforcer} to validate entitlements before gameplay begins.
     *
     * @param event Forge lifecycle event exposing the dedicated server instance
     */
    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        LOGGER.info(DIAGNOSTICS, "[{}] ServerStarting", MODID);
        // Resolve the dedicated server's root directory -> .../config/hellas/license.json
        java.nio.file.Path serverRoot = event.getServer().getServerDirectory().toPath();

        if (AutoBisectRunner.maybeRun(serverRoot)) {
            LOGGER.info(DIAGNOSTICS, "[{}] Auto-bisect completed; shutting down server.", MODID);
            event.getServer().halt(true);
            return;
        }

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

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isClientSide()) return;
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            LOGGER.info(DIAGNOSTICS, "[{}] WorldLoad {}", MODID, world.getDimensionKey().location());
        } else {
            LOGGER.info(DIAGNOSTICS, "[{}] WorldLoad {}", MODID, event.getWorld());
        }
    }

    private static void logModList() {
        List<ModInfo> mods = ModList.get().getMods();
        String summary = mods.stream()
                .map(mod -> mod.getModId() + ":" + mod.getVersion().toString())
                .collect(Collectors.joining(", "));
        String hellasSummary = mods.stream()
                .filter(mod -> mod.getModId().startsWith("hellas"))
                .map(mod -> mod.getModId() + ":" + mod.getVersion().toString())
                .collect(Collectors.joining(", "));
        LOGGER.info(DIAGNOSTICS, "[{}] Active mods ({}): {}", MODID, mods.size(), summary);
        if (!hellasSummary.isEmpty()) {
            LOGGER.info(DIAGNOSTICS, "[{}] Hellas suite versions: {}", MODID, hellasSummary);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryDiagnostics {
        @SubscribeEvent
        public static void onRegistryRegister(RegistryEvent.Register<?> event) {
            LOGGER.info(DIAGNOSTICS, "[{}] Registry event: {}", MODID, event.getRegistry().getRegistryName());
        }
    }

    /**
     * Indicates whether the mod finished common setup. Sidemods can call this
     * to guard API usage that requires HellasControl to be available.
     *
     * @return {@code true} once {@link #onCommonSetup(FMLCommonSetupEvent)} completed
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Client-side helper used by GUI hooks to surface whether the remote
     * server is licensed. The information is populated via the mod handshake.
     *
     * @return {@code true} only if the server both runs HellasControl and owns a valid license
     */
    public static boolean isServerLicensedClientSide() {
        return com.xsasakihaise.hellascontrol.ClientModState.isServerLicensed();
    }

    /**
     * Checks whether the cached license includes a specific entitlement string.
     * This is the common helper used by sidemods to gate their own startup
     * routines.
     *
     * @param key entitlement identifier such as {@code "wilds"}
     * @return {@code true} when the key is present on the current license
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
     * Convenience guard that throws when the provided entitlement is missing
     * from the active server license. Designed for sidemods that want to fail
     * fast during construction or setup rather than continuing in an invalid
     * state.
     *
     * @param key entitlement identifier that must be present
     * @throws IllegalStateException if the entitlement is absent or the license cache is invalid
     */
    public static void requireEntitlement(String key) {
        if (!hasEntitlement(key)) {
            throw new IllegalStateException("HellasControl: Missing entitlement '" + key + "'.");
        }
    }
}
