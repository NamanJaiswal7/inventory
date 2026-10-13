package com.warehouse.optimized;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * PATTERN: Strategy / Registry Dispatcher (Eliminating switch statements)
 * ============================================================================
 * Uses Spring's automatic collection injection to discover all concrete
 * ShipmentReceiver implementations available on the classpath.
 *
 * This completely satisfies the Open/Closed Principle (OCP):
 * When a developer adds a new receiver (e.g., `FurnitureReceiver`), they simply
 * annotate it with `@Component`. This registry discovers it automatically
 * WITHOUT requiring any code edits in this class or in any controller.
 */
@Component
public class ShipmentReceiverRegistry {

    private final Map<String, ShipmentReceiver> receiverMap;

    public ShipmentReceiverRegistry(List<ShipmentReceiver> receivers) {
        this.receiverMap = receivers.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                r -> r.getType().toLowerCase().trim(),
                                Function.identity()
                        ),
                        Collections::unmodifiableMap
                ));
    }

    /**
     * Resolves the appropriate ShipmentReceiver by product type string.
     *
     * @param type product type name (case-insensitive)
     * @return concrete ShipmentReceiver instance
     * @throws IllegalArgumentException if type is unsupported
     */
    public ShipmentReceiver getReceiver(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Product type cannot be null or empty.");
        }

        ShipmentReceiver receiver = receiverMap.get(type.toLowerCase().trim());
        if (receiver == null) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported product type: '%s'. Supported types are: %s",
                    type, receiverMap.keySet()
            ));
        }

        return receiver;
    }

    /**
     * Returns the set of all registered shipment product types.
     */
    public Set<String> getSupportedTypes() {
        return receiverMap.keySet();
    }
}
