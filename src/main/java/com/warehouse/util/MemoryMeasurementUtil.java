package com.warehouse.util;

/**
 * Utility for capturing accurate JVM memory readings.
 * 
 * Invokes System.gc() before snapshots to collect transient garbage,
 * allowing accurate measurement of retained heap objects.
 */
public final class MemoryMeasurementUtil {

    private MemoryMeasurementUtil() {
        // utility class
    }

    /**
     * Attempts garbage collection and pauses briefly to let GC cycles complete,
     * then calculates the currently consumed heap memory.
     *
     * @return current used memory in bytes (totalMemory - freeMemory)
     */
    public static long getUsedMemoryWithGc() {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Converts a byte count to Megabytes (MB) rounded to two decimal places.
     */
    public static double bytesToMB(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        return Math.round(mb * 100.0) / 100.0;
    }
}
