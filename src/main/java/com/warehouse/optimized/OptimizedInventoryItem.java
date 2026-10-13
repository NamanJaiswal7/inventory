package com.warehouse.optimized;

/**
 * ============================================================================
 * PATTERN: Flyweight Context
 * ============================================================================
 * Represents a physical box on a warehouse shelf.
 *
 * Stores ONLY the extrinsic, unique state:
 *  - barcode (unique to this physical box)
 *  - location (aisle and shelf where this box sits)
 *
 * AND holds a lightweight 64-bit object reference to the shared Flyweight
 * (ProductCatalogItem). It DOES NOT allocate or store its own 5MB payload.
 *
 * Memory footprint: ~32-48 bytes (just object header and 3 references),
 * compared to 5,242,880 bytes in the Naive implementation.
 */
public class OptimizedInventoryItem {

    // Extrinsic state (unique per box)
    private final String barcode;
    private final String location;

    // Lightweight reference to the shared intrinsic Flyweight (ProductCatalogItem)
    private final ProductCatalogItem productCatalogItem;

    public OptimizedInventoryItem(String barcode, String location, ProductCatalogItem productCatalogItem) {
        if (productCatalogItem == null) {
            throw new IllegalArgumentException("ProductCatalogItem flyweight reference cannot be null.");
        }
        this.barcode = barcode;
        this.location = location;
        this.productCatalogItem = productCatalogItem;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLocation() {
        return location;
    }

    public ProductCatalogItem getProductCatalogItem() {
        return productCatalogItem;
    }

    // Delegating helper methods to access intrinsic catalog details
    public String getSku() {
        return productCatalogItem.getSku();
    }

    public String getName() {
        return productCatalogItem.getName();
    }

    public byte[] getHeavyImageData() {
        return productCatalogItem.getHeavyImageData();
    }

    @Override
    public String toString() {
        return "OptimizedInventoryItem{" +
                "barcode='" + barcode + '\'' +
                ", location='" + location + '\'' +
                ", sku='" + productCatalogItem.getSku() + '\'' +
                ", name='" + productCatalogItem.getName() + '\'' +
                ", flyweightRef=" + Integer.toHexString(System.identityHashCode(productCatalogItem)) +
                '}';
    }
}
