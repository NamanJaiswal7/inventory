package com.warehouse.optimized;

import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * PATTERN: Factory Method (Concrete Creator - Books)
 * ============================================================================
 * Implements createInventoryItem() to resolve the Books Flyweight from the
 * ProductCatalog and produce an OptimizedInventoryItem.
 */
@Component
public class BooksReceiver extends ShipmentReceiver {

    public static final String TYPE = "books";
    public static final String SKU = "BOOK-DESIGN-PATTERNS-GOF";
    public static final String NAME = "Design Patterns: Elements of Reusable Object-Oriented Software";

    private final ProductCatalog productCatalog;

    public BooksReceiver(ProductCatalog productCatalog) {
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
