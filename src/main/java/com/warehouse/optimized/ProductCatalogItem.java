package com.warehouse.optimized;

import java.util.concurrent.ThreadLocalRandom;

/**
 * ============================================================================
 * PATTERN: Flyweight (Intrinsic State)
 * ============================================================================
 * Stores immutable, context-independent data shared across all physical boxes
 * of a given product catalog entry:
 *  - SKU (Stock Keeping Unit)
 *  - Product Name
 *  - Heavy 5MB image/metadata payload
 *
 * This object is immutable and stateless with respect to physical warehouse locations.
 * Regardless of whether 1 box or 1,000,000 boxes exist in the warehouse, only ONE
 * instance of this class is created in memory per SKU.
 */
public class ProductCatalogItem {

    private final String sku;
    private final String name;

    public static final int HEAVY_PAYLOAD_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private final byte[] heavyImageData;

    public ProductCatalogItem(String sku, String name) {
        this.sku = sku;
        this.name = name;

        // Allocate the 5MB payload buffer ONCE per unique SKU
        this.heavyImageData = new byte[HEAVY_PAYLOAD_SIZE_BYTES];
        ThreadLocalRandom.current().nextBytes(this.heavyImageData);
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
        return "ProductCatalogItem{" +
                "sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", heavyPayloadBytes=" + heavyImageData.length +
                '}';
    }
}
