package com.warehouse.optimized;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * PATTERN: Factory Method (The Creator)
 * ============================================================================
 * Defines the core shipment processing workflow while delegating the creation
 * of the concrete OptimizedInventoryItem to subclasses via the factory method
 * `createInventoryItem(...)`.
 *
 * Benefits:
 * 1. Open/Closed Principle (OCP): New shipment types can be introduced by creating
 *    new subclasses of ShipmentReceiver without modifying existing receiver code.
 * 2. Separation of Concerns: The high-level workflow (looping, generating barcodes,
 *    assigning locations) is defined once in `processShipment`, while specific
 *    catalog bindings are handled polymorphically in concrete creators.
 */
public abstract class ShipmentReceiver {

    /**
     * The Factory Method.
     * Concrete creators implement this method to instantiate the appropriate
     * inventory item, resolving the shared Flyweight from the ProductCatalog.
     *
     * @param barcode  extrinsic barcode for the physical box
     * @param location extrinsic warehouse shelf location
     * @return a lightweight OptimizedInventoryItem holding extrinsic state and Flyweight reference
     */
    protected abstract OptimizedInventoryItem createInventoryItem(String barcode, String location);

    /**
     * Returns the shipment type identifier supported by this receiver
     * (e.g., "electronics", "books", "clothing").
     */
    public abstract String getType();

    /**
     * High-level template method that processes an incoming batch of items.
     * It relies on the Factory Method to produce individual items.
     *
     * @param count number of boxes arriving in this shipment
     * @return list of processed OptimizedInventoryItem contexts
     */
    public List<OptimizedInventoryItem> processShipment(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Shipment count cannot be negative.");
        }

        List<OptimizedInventoryItem> items = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String barcode = "BAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String location = "AISLE-" + (i / 50 + 1) + "-SHELF-" + (i % 50 + 1);

            // Polymorphic call to the Factory Method
            OptimizedInventoryItem item = createInventoryItem(barcode, location);
            items.add(item);
        }

        return items;
    }
}
