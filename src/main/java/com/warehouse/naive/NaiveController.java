package com.warehouse.naive;

import com.warehouse.dto.ShipmentReceiptResponse;
import com.warehouse.util.MemoryMeasurementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller exposing the Naive Shipment Ingestion endpoint.
 *
 * Demonstrates:
 * 1. Linear memory bloat proportional to item count: O(N * 5MB).
 * 2. High risk of java.lang.OutOfMemoryError when count is large.
 */
@RestController
@RequestMapping("/api/v1/naive")
public class NaiveController {

    private static final Logger log = LoggerFactory.getLogger(NaiveController.class);

    private final NaiveShipmentProcessor shipmentProcessor;

    public NaiveController(NaiveShipmentProcessor shipmentProcessor) {
        this.shipmentProcessor = shipmentProcessor;
    }

    /**
     * Ingests a warehouse shipment using the Naive approach.
     *
     * Example:
     * POST /api/v1/naive/receive?type=electronics&count=50
     * POST /api/v1/naive/receive?type=electronics&count=1000
     */
    @PostMapping("/receive")
    public ResponseEntity<ShipmentReceiptResponse> receiveShipment(
            @RequestParam(defaultValue = "electronics") String type,
            @RequestParam(defaultValue = "1000") int count) {

        log.info("Starting NAIVE shipment receipt: type={}, count={}", type, count);

        long startTime = System.currentTimeMillis();

        // 1. Stabilize heap and capture baseline memory
        long memoryBefore = MemoryMeasurementUtil.getUsedMemoryWithGc();

        List<NaiveInventoryItem> inventoryItems = new ArrayList<>();

        try {
            // 2. Process shipment and hold strong references to items in list
            inventoryItems = shipmentProcessor.processShipment(type, count);

            // 3. Stabilize heap and capture final retained memory
            long memoryAfter = MemoryMeasurementUtil.getUsedMemoryWithGc();
            long memoryDelta = Math.max(0, memoryAfter - memoryBefore);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            double payloadMb = MemoryMeasurementUtil.bytesToMB(NaiveInventoryItem.HEAVY_PAYLOAD_SIZE_BYTES);
            double deltaMb = MemoryMeasurementUtil.bytesToMB(memoryDelta);

            List<String> sample = inventoryItems.stream()
                    .limit(3)
                    .map(item -> item.getBarcode() + " @ " + item.getLocation() + " (" + item.getName() + ")")
                    .collect(Collectors.toList());

            ShipmentReceiptResponse response = new ShipmentReceiptResponse(
                    "SUCCESS",
                    "NAIVE (No Patterns)",
                    type,
                    inventoryItems.size(),
                    inventoryItems.size(), // Every item has its own distinct heavy 5MB array!
                    payloadMb,
                    memoryDelta,
                    deltaMb,
                    MemoryMeasurementUtil.bytesToMB(memoryBefore),
                    MemoryMeasurementUtil.bytesToMB(memoryAfter),
                    executionTimeMs,
                    String.format("Created %d naive items. Allocated %d separate 5MB payload buffers (~%.2f MB total).",
                            inventoryItems.size(), inventoryItems.size(), deltaMb),
                    sample
            );

            log.info("Completed NAIVE processing: created {} items, delta={} MB", inventoryItems.size(), deltaMb);
            return ResponseEntity.ok(response);

        } catch (OutOfMemoryError oom) {
            // Explicitly caught to provide telemetry on heap exhaustion
            long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long executionTimeMs = System.currentTimeMillis() - startTime;

            log.error("OUT OF MEMORY occurred during NAIVE shipment processing after {} items", inventoryItems.size(), oom);

            ShipmentReceiptResponse errorResponse = new ShipmentReceiptResponse(
                    "OUT_OF_MEMORY_ERROR",
                    "NAIVE (No Patterns)",
                    type,
                    inventoryItems.size(),
                    inventoryItems.size(),
                    MemoryMeasurementUtil.bytesToMB(NaiveInventoryItem.HEAVY_PAYLOAD_SIZE_BYTES),
                    memoryAfter - memoryBefore,
                    MemoryMeasurementUtil.bytesToMB(memoryAfter - memoryBefore),
                    MemoryMeasurementUtil.bytesToMB(memoryBefore),
                    MemoryMeasurementUtil.bytesToMB(memoryAfter),
                    executionTimeMs,
                    String.format("Heap exhausted! Created %d items before throwing OutOfMemoryError. " +
                            "Each item allocated 5MB independently without Flyweight sharing.", inventoryItems.size()),
                    List.of()
            );

            // Free temporary list reference to help JVM recover
            inventoryItems.clear();
            System.gc();

            return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(errorResponse);
        }
    }
}
