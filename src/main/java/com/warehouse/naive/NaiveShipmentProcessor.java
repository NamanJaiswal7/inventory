package com.warehouse.naive;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * ANTI-PATTERN: The Coupling Problem (No Factory Method)
 * ============================================================================
 * This service directly decides how to instantiate every product type using a
 * tightly-coupled, rigid conditional block (switch / if-else).
 *
 * Flaws:
 * 1. Violates the Open/Closed Principle (OCP): Whenever the warehouse expands
 *    to handle "Toys", "Groceries", or "Automotive", this class MUST be modified.
 * 2. High Coupling: The processor is directly bound to specific SKUs, product names,
 *    and construction details of every product category.
 * 3. Violates Single Responsibility Principle: Handles both shipment iteration logic
 *    and product-specific instantiation logic.
 */
@Service
public class NaiveShipmentProcessor {

    /**
     * Processes shipment by directly evaluating product types via switch/case.
     *
     * @param type  the category of product (electronics, books, clothing)
     * @param count the number of boxes arriving in this shipment
     * @return list of newly created NaiveInventoryItem objects
     */
    public List<NaiveInventoryItem> processShipment(String type, int count) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Product type cannot be null or empty.");
        }

        List<NaiveInventoryItem> items = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String barcode = "BAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String location = "AISLE-" + (i / 50 + 1) + "-SHELF-" + (i % 50 + 1);

            // ANTI-PATTERN: Tightly coupled conditional instantiation
            NaiveInventoryItem item;
            switch (type.trim().toLowerCase()) {
                case "electronics":
                    item = new NaiveInventoryItem(
                            barcode,
                            location,
                            "ELEC-SONY-WH1000XM5",
                            "Sony Wireless Noise Canceling Headphones"
                    );
                    break;

                case "books":
                    item = new NaiveInventoryItem(
                            barcode,
                            location,
                            "BOOK-DESIGN-PATTERNS-GOF",
                            "Design Patterns: Elements of Reusable Object-Oriented Software"
                    );
                    break;

                case "clothing":
                    item = new NaiveInventoryItem(
                            barcode,
                            location,
                            "CLOTH-HOODIE-FLEECE-BLK",
                            "Premium Fleece Heavyweight Zip Hoodie"
                    );
                    break;

                default:
                    // Every new product type requires modifying this file!
                    throw new IllegalArgumentException("Unsupported product type: '" + type +
                            "'. Supported types are: electronics, books, clothing.");
            }

            items.add(item);
        }

        return items;
    }
}
