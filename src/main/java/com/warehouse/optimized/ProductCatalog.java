package com.warehouse.optimized;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ============================================================================
 * PATTERN: Flyweight Factory
 * ============================================================================
 * A thread-safe registry that creates and manages Flyweight instances.
 *
 * It ensures that Flyweights are shared properly: when a client requests a
 * ProductCatalogItem, the factory returns an existing instance if present,
 * or instantiates and stores a new one if it does not yet exist.
 *
 * Guarantees:
 * - Thread-safety via ConcurrentHashMap.
 * - Atomic computation via computeIfAbsent, ensuring exactly ONE 5MB allocation
 *   per unique SKU under high concurrency.
 */
@Component
public class ProductCatalog {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalog.class);

    private final ConcurrentMap<String, ProductCatalogItem> catalogRegistry = new ConcurrentHashMap<>();

    /**
     * Retrieves an existing ProductCatalogItem or creates and caches a new one.
     *
     * @param sku  the unique Stock Keeping Unit identifier
     * @param name product display name used if creation is required
     * @return the shared, cached ProductCatalogItem flyweight
     */
    public ProductCatalogItem getOrCreateProduct(String sku, String name) {
        return catalogRegistry.computeIfAbsent(sku, key -> {
            log.info(">>> [FLYWEIGHT ALLOCATION] Creating 5MB ProductCatalogItem for SKU: '{}' ('{}')", key, name);
            return new ProductCatalogItem(key, name);
        });
    }

    /**
     * Checks if a product SKU is already cached in memory.
     */
    public boolean containsSku(String sku) {
        return catalogRegistry.containsKey(sku);
    }

    /**
     * Returns the total count of distinct heavy Flyweights currently stored in heap memory.
     */
    public int getCatalogSize() {
        return catalogRegistry.size();
    }

    /**
     * Clears the catalog registry (used primarily for benchmarking and testing).
     */
    public void clear() {
        catalogRegistry.clear();
    }
}
