package de.myownbrain.autoLogout.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;


public final class TPSMonitor {
    private static final double TARGET_TPS = 20.0;
    private static final double TARGET_MSPT = 1000.0 / TARGET_TPS;

    private static final int TICK_TIME_ARRAY_SIZE = 100;
    private static final int MIN_MULTIPLAYER_SAMPLES = 3;
    private static final int SAMPLE_BUFFER_SIZE = 5;
    private static final long STALE_THRESHOLD_MS = 10_000L;

    // Multiplayer estimation state
    private static final long[] packetArrivalMs = new long[SAMPLE_BUFFER_SIZE];
    private static final long[] packetGameTick = new long[SAMPLE_BUFFER_SIZE];
    private static int sampleHead = 0;
    private static int sampleCount = 0;

    // Cached results (updated lazily)
    private static double cachedTps = TARGET_TPS;
    private static double cachedMspt = TARGET_MSPT;

    // clear when the player switches servers/worlds
    public static void reset() {
        sampleHead = 0;
        sampleCount = 0;
        cachedTps = TARGET_TPS;
        cachedMspt = TARGET_MSPT;
    }

    // Multiplayer packet hook
    public static void onTimePacketReceived(long gameTime) {
        // Record wall-clock time in milliseconds
        long nowMs = System.currentTimeMillis();

        packetArrivalMs[sampleHead] = nowMs;
        packetGameTick[sampleHead] = gameTime;
        sampleHead = (sampleHead + 1) % SAMPLE_BUFFER_SIZE;
        if (sampleCount < SAMPLE_BUFFER_SIZE) sampleCount++;

        // Recompute rolling average if we have enough data.
        if (sampleCount >= MIN_MULTIPLAYER_SAMPLES) recomputeMultiplayer();
    }

    // Public accessors
    public static double getTps() {
        if (isIntegratedServerRunning()) return computeSingleplayerTps();
        checkStaleness();
        return cachedTps;
    }

    public static double getMspt() {
        if (isIntegratedServerRunning()) {
            return computeSingleplayerMspt();
        }
        checkStaleness();
        return cachedMspt;
    }

    public static boolean hasData() {
        if (isIntegratedServerRunning()) return true;
        return sampleCount >= MIN_MULTIPLAYER_SAMPLES && !isStale();
    }

    // Singleplayer implementation
    private static double computeSingleplayerMspt() {
        MinecraftServer server = getSingleplayerServer();
        if (server == null) return TARGET_MSPT;

        long[] tickTimes = server.getTickTimesNanos();
        int filled = Math.min(server.getTickCount(), TICK_TIME_ARRAY_SIZE);
        if (filled == 0) return TARGET_MSPT;

        long sum = 0L;
        for (int i = 0; i < filled; i++) sum += tickTimes[i];

        double avgNanos = (double) sum / filled;
        return avgNanos / 1_000_000.0; // ns → ms
    }

    private static double computeSingleplayerTps() {
        MinecraftServer server = getSingleplayerServer();
        if (server == null) return TARGET_TPS;

        float tickRate = server.tickRateManager().tickrate();
        double mspt = computeSingleplayerMspt();
        double calculatedTps = Math.min(TARGET_TPS, 1000.0 / mspt);
        return tickRate < calculatedTps ? tickRate : calculatedTps;
    }

    // Multiplayer estimation implementation
    private static void recomputeMultiplayer() {
        int oldestIdx = (sampleHead) % SAMPLE_BUFFER_SIZE; // next-to-be-overwritten slot
        if (sampleCount < SAMPLE_BUFFER_SIZE) oldestIdx = 0;
        int newestIdx = (sampleHead - 1 + SAMPLE_BUFFER_SIZE) % SAMPLE_BUFFER_SIZE;

        long wallDeltaMs = packetArrivalMs[newestIdx] - packetArrivalMs[oldestIdx]; // elapsed "wall clock" (real world) time
        long tickDelta = packetGameTick[newestIdx] - packetGameTick[oldestIdx]; // elapsed ingame ticks

        // Degenerate case: time went backwards or game-time did not advance (e.g. /time set).
        if (tickDelta <= 0 || wallDeltaMs <= 0) return;

        double mspt = (double) wallDeltaMs / tickDelta;
        double tps = 1000.0 / mspt;

        cachedMspt = mspt;
        cachedTps = tps;
    }


    // Staleness guard
    private static boolean isStale() {
        if (sampleCount == 0) return true;
        int newestIdx = (sampleHead - 1 + SAMPLE_BUFFER_SIZE) % SAMPLE_BUFFER_SIZE;
        return (System.currentTimeMillis() - packetArrivalMs[newestIdx]) > STALE_THRESHOLD_MS;
    }


    private static void checkStaleness() {
        if (isStale()) {
            cachedTps = TARGET_TPS;
            cachedMspt = TARGET_MSPT;
        }
    }

    // Helpers
    private static boolean isIntegratedServerRunning() {
        return getSingleplayerServer() != null;
    }

    @Nullable
    private static MinecraftServer getSingleplayerServer() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    // Handler
    public static void monitorTPS() {
        Minecraft client = Minecraft.getInstance();

        if (ConfigManager.isModEnabled && ConfigManager.isTPSTrackingEnabled && client.player != null) {
            double tps = getTps();
            if (tps <= ConfigManager.tpsThreshold) {
                client.player.connection.getConnection().disconnect(LogoutScreen.createTPSScreen());
                ConfigManager.isTPSTrackingEnabled = false;
                ConfigManager.saveConfig();
            }
        }
    }
}