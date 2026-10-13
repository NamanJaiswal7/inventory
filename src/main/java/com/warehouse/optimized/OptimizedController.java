package com.warehouse.optimized;

import com.warehouse.dto.ShipmentReceiptResponse;
import com.warehouse.util.MemoryMeasurementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller exposing the Optimized Shipment Ingestion endpoint.
 *
 * Demonstrates:
 * 1. Constant memory footprint for catalog payloads: O(1 * 5MB) regardless of N items.
 * 2. Polymorphic dispatch through ShipmentReceiverRegistry without switch statements.
 */
@RestController
@RequestMapping("/api/v1/optimized")
public class OptimizedController {

    private static final Logger log = LoggerFactory.getLogger(OptimizedController.class);

    private final ShipmentReceiverRegistry receiverRegistry;
    private final ProductCatalog productCatalog;

    public OptimizedController(ShipmentReceiverRegistry receiverRegistry, ProductCatalog productCatalog) {
        this.receiverRegistry = receiverRegistry;
        this.productCatalog = productCatalog;
    }

    /**
     * Ingests a warehouse shipment using Flyweight and Factory Method patterns.
     *
     * Example:
     * POST /api/v1/optimized/receive?type=electronics&count=1000
     */
    @PostMapping("/receive")
    public ResponseEntity<ShipmentReceiptResponse> receiveShipment(
            @RequestParam(defaultValue = "electronics") String type,
            @RequestParam(defaultValue = "1000") int count) {

        log.info("Starting OPTIMIZED shipment receipt: type={}, count={}", type, count);

        // 1. Resolve creator via Strategy/Registry pattern (No switch statement!)
        ShipmentReceiver receiver = receiverRegistry.getReceiver(type);

        long startTime = System.currentTimeMillis();

        // 2. Stabilize heap and capture baseline memory
        long memoryBefore = MemoryMeasurementUtil.getUsedMemoryWithGc();

        // 3. Process shipment via Factory Method template method
        List<OptimizedInventoryItem> items = receiver.processShipment(count);

        // 4. Stabilize heap and capture final memory
        long memoryAfter = MemoryMeasurementUtil.getUsedMemoryWithGc();
        long memoryDelta = Math.max(0, memoryAfter - memoryBefore);
        long executionTimeMs = System.currentTimeMillis() - startTime;

        double payloadMb = MemoryMeasurementUtil.bytesToMB(ProductCatalogItem.HEAVY_PAYLOAD_SIZE_BYTES);
        double deltaMb = MemoryMeasurementUtil.bytesToMB(memoryDelta);

        // Inspect flyweight catalog: regardless of count (e.g. 1,000), catalog contains only 1 entry for this SKU
        int catalogEntries = productCatalog.getCatalogSize();

        List<String> sample = items.stream()
                .limit(3)
                .map(item -> item.getBarcode() + " @ " + item.getLocation() + " (" + item.getName() + ")")
                .collect(Collectors.toList());

        ShipmentReceiptResponse response = new ShipmentReceiptResponse(
                "SUCCESS",
                "OPTIMIZED (Flyweight + Factory Method)",
                type,
                items.size(),
                catalogEntries, // Total unique 5MB Flyweight objects in memory
                payloadMb,
                memoryDelta,
                deltaMb,
                MemoryMeasurementUtil.bytesToMB(memoryBefore),
                MemoryMeasurementUtil.bytesToMB(memoryAfter),
                executionTimeMs,
                String.format("Successfully created %d lightweight context items. " +
                                "The 5MB payload was instantiated exactly ONCE in ProductCatalog. " +
                                "Retained memory delta: ~%.2f MB.",
                        items.size(), deltaMb),
                sample
        );

        log.info("Completed OPTIMIZED processing: created {} items, flyweights={}, delta={} MB",
                items.size(), catalogEntries, deltaMb);

        return ResponseEntity.ok(response);
    }
}
