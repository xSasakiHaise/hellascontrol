package com.xsasakihaise.hellascontrol.enforcement;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public final class PiracyTrapScheduler {
    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger(PiracyTrapScheduler.class);
    private static final String REQUIRED_SUBSTRING = "crafted by the Hephaestus Forge";
    private static final Duration INITIAL_DELAY = Duration.ofMinutes(2);
    private static final Duration MIN_DELAY = Duration.ofMinutes(15);
    private static final Duration MAX_DELAY = Duration.ofMinutes(45);
    private static final Duration GLOBAL_THROTTLE = Duration.ofMinutes(10);
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "hellascontrol-piracy-trap");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
    private static volatile long lastBroadcastAtMs = 0L;

    private PiracyTrapScheduler() {}

    public static void register() {
        // No-op to force class loading for the EventBusSubscriber.
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        MinecraftServer server = event.getEntity().getServer();
        if (server == null || !server.isDedicatedServer()) {
            return;
        }
        if (!SCHEDULED.compareAndSet(false, true)) {
            return;
        }
        EXECUTOR.schedule(() -> evaluateAndSchedule(server), INITIAL_DELAY.toMinutes(), TimeUnit.MINUTES);
    }

    private static void evaluateAndSchedule(MinecraftServer server) {
        List<IModInfo> hellasMods = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            if (mod.getModId().startsWith("hellas")) {
                hellasMods.add(mod);
            }
        }
        hellasMods.sort(Comparator.comparing(IModInfo::getModId));
        boolean suspectedPirate = false;
        for (IModInfo mod : hellasMods) {
            String description = mod.getDescription();
            if (description == null || !description.contains(REQUIRED_SUBSTRING)) {
                suspectedPirate = true;
                break;
            }
        }
        if (!suspectedPirate) {
            LOGGER.info("[HellasControl] Piracy trap check passed (metadata OK).");
            return;
        }
        LOGGER.info("[HellasControl] Piracy trap active: missing metadata substring detected.");
        for (int i = 0; i < hellasMods.size(); i++) {
            IModInfo mod = hellasMods.get(i);
            int index = i + 1;
            scheduleNextBroadcast(server, mod.getModId(), index);
        }
    }

    private static void scheduleNextBroadcast(MinecraftServer server, String modId, int index) {
        long delayMinutes = ThreadLocalRandom.current().nextLong(MIN_DELAY.toMinutes(), MAX_DELAY.toMinutes() + 1);
        EXECUTOR.schedule(() -> {
            if (server == null || server.isStopped()) {
                return;
            }
            long now = System.currentTimeMillis();
            long elapsed = now - lastBroadcastAtMs;
            if (elapsed < GLOBAL_THROTTLE.toMillis()) {
                long remainingMs = GLOBAL_THROTTLE.toMillis() - elapsed;
                EXECUTOR.schedule(() -> broadcastAndReschedule(server, modId, index),
                        remainingMs, TimeUnit.MILLISECONDS);
                return;
            }
            broadcastAndReschedule(server, modId, index);
        }, delayMinutes, TimeUnit.MINUTES);
    }

    private static void broadcastAndReschedule(MinecraftServer server, String modId, int index) {
        if (server == null || server.isStopped()) {
            return;
        }
        long now = System.currentTimeMillis();
        lastBroadcastAtMs = now;
        String msg = "this server is pirating Software from the Hephaestus forge. Pls Contact the dev via "
                + "https://web.hephaestus-forge.cc/about/legal/ or https://discord.pixelmon-server.com"
                + " (mod=" + modId + " index=" + index + ")";
        server.execute(() -> server.getPlayerList().broadcastSystemMessage(Component.literal(msg), false));
        scheduleNextBroadcast(server, modId, index);
    }
}
