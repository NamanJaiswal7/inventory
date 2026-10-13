package com.warehouse.optimized;

import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * PATTERN: Factory Method (Concrete Creator - Electronics)
 * ============================================================================
 * Implements createInventoryItem() to resolve the Electronics Flyweight from the
 * ProductCatalog and produce an OptimizedInventoryItem.
 */
@Component
public class ElectronicsReceiver extends ShipmentReceiver {

    public static final String TYPE = "electronics";
    public static final String SKU = "ELEC-SONY-WH1000XM5";
    public static final String NAME = "Sony Wireless Noise Canceling Headphones";

    private final ProductCatalog productCatalog;

    public ElectronicsReceiver(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected OptimizedInventoryItem createInventoryItem(String barcode, String location) {
        // Retrieve the shared 5MB Flyweight from the catalog (created only once)
        ProductCatalogItem flyweight = productCatalog.getOrCreateProduct(SKU, NAME);

        // Return the lightweight context wrapping barcode, location, and the flyweight reference
        return new OptimizedInventoryItem(barcode, location, flyweight);
    }
}
