package com.warehouse.naive;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ============================================================================
 * ANTI-PATTERN: The Memory Problem (No Flyweight)
 * ============================================================================
 * This class conflates extrinsic state (unique to each physical box: barcode,
 * location) with intrinsic state (shared catalog data: SKU, name, heavy image).
 *
 * Consequence:
 * Every single physical box stored on a warehouse shelf creates a completely
 * redundant 5MB byte array in memory. If 1,000 boxes of the same headphones are
 * received, this class allocates:
 *      1,000 * 5 MB = 5,000 MB (~5 GB) of JVM heap space!
 */
public class NaiveInventoryItem {

    // Unique / Extrinsic state (varies per physical box)
    private final String barcode;
    private final String location;

    // Redundant / Intrinsic state (identical for all items of this product)
    private final String sku;
    private final String name;

    // Heavy 5MB payload duplicated across every single instance
    public static final int HEAVY_PAYLOAD_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private final byte[] heavyImageData;

    public NaiveInventoryItem(String barcode, String location, String sku, String name) {
        this.barcode = barcode;
        this.location = location;
        this.sku = sku;
        this.name = name;

        // Allocate a dedicated 5MB buffer for EACH item instance
        this.heavyImageData = new byte[HEAVY_PAYLOAD_SIZE_BYTES];
        // Populate sample bytes so JVM doesn't eliminate allocation
        ThreadLocalRandom.current().nextBytes(this.heavyImageData);
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLocation() {
        return location;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public byte[] getHeavyImageData() {
        return heavyImageData;
    }

    @Override
    public String toString() {
        return "NaiveInventoryItem{" +
                "barcode='" + barcode + '\'' +
                ", location='" + location + '\'' +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", payloadSize=" + heavyImageData.length + " bytes" +
                '}';
    }
}
