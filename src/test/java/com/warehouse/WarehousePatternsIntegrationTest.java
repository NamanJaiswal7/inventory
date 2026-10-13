package com.warehouse;

import com.warehouse.naive.NaiveInventoryItem;
import com.warehouse.naive.NaiveShipmentProcessor;
import com.warehouse.optimized.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WarehousePatternsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductCatalog productCatalog;

    @Autowired
    private ShipmentReceiverRegistry receiverRegistry;

    @Autowired
    private ElectronicsReceiver electronicsReceiver;

    @Autowired
    private NaiveShipmentProcessor naiveShipmentProcessor;

    @BeforeEach
    void setUp() {
        productCatalog.clear();
    }

    @Test
    @DisplayName("Flyweight: ProductCatalog creates exactly one instance per SKU")
    void testFlyweightSingletonPerSku() {
        ProductCatalogItem first = productCatalog.getOrCreateProduct("TEST-SKU", "Test Product");
        ProductCatalogItem second = productCatalog.getOrCreateProduct("TEST-SKU", "Test Product");

        assertNotNull(first);
        assertNotNull(second);
        assertSame(first, second, "Both references must point to the identical Flyweight in heap memory");
        assertEquals(1, productCatalog.getCatalogSize(), "Catalog size must be exactly 1");
    }

    @Test
    @DisplayName("Factory Method: ElectronicsReceiver instantiates items with unique barcodes sharing one Flyweight")
    void testFactoryMethodProcessing() {
        int count = 50;
        List<OptimizedInventoryItem> items = electronicsReceiver.processShipment(count);

        assertEquals(count, items.size());
        assertEquals(1, productCatalog.getCatalogSize(), "Catalog must only have 1 Flyweight for all 50 items");

        ProductCatalogItem expectedFlyweight = items.get(0).getProductCatalogItem();
        for (OptimizedInventoryItem item : items) {
            assertSame(expectedFlyweight, item.getProductCatalogItem(),
                    "Every physical item must reference the same shared Flyweight");
            assertNotNull(item.getBarcode());
            assertNotNull(item.getLocation());
        }

        // Verify barcodes are unique
        long uniqueBarcodes = items.stream().map(OptimizedInventoryItem::getBarcode).distinct().count();
        assertEquals(count, uniqueBarcodes, "Every physical item must have a unique barcode");
    }

    @Test
    @DisplayName("Strategy/Registry: Resolves receivers dynamically without switch statements")
    void testReceiverRegistryResolution() {
        ShipmentReceiver receiver = receiverRegistry.getReceiver("electronics");
        assertInstanceOf(ElectronicsReceiver.class, receiver);

        ShipmentReceiver books = receiverRegistry.getReceiver("books");
        assertInstanceOf(BooksReceiver.class, books);

        ShipmentReceiver clothing = receiverRegistry.getReceiver("clothing");
        assertInstanceOf(ClothingReceiver.class, clothing);

        assertThrows(IllegalArgumentException.class, () -> receiverRegistry.getReceiver("invalid-category"));
    }

    @Test
    @DisplayName("Naive Comparison: Creates separate byte arrays for every single item")
    void testNaiveSeparateAllocations() {
        int count = 2;
        List<NaiveInventoryItem> items = naiveShipmentProcessor.processShipment("electronics", count);

        assertEquals(2, items.size());
        assertNotSame(items.get(0).getHeavyImageData(), items.get(1).getHeavyImageData(),
                "Naive approach duplicates the 5MB array across instances");
    }

    @Test
    @DisplayName("REST API: POST /api/v1/optimized/receive returns SUCCESS and reports 1 catalog instance")
    void testOptimizedEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/optimized/receive")
                        .param("type", "electronics")
                        .param("count", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.implementation").value("OPTIMIZED (Flyweight + Factory Method)"))
                .andExpect(jsonPath("$.itemCount").value(100))
                .andExpect(jsonPath("$.catalogInstancesCreated").value(1))
                .andExpect(jsonPath("$.sampleItems", hasSize(3)));
    }

    @Test
    @DisplayName("REST API: POST /api/v1/naive/receive returns SUCCESS for smaller count")
    void testNaiveEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/naive/receive")
                        .param("type", "electronics")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.itemCount").value(5))
                .andExpect(jsonPath("$.catalogInstancesCreated").value(5));
    }

    @Test
    @DisplayName("REST API: Invalid product type returns 400 Bad Request with error details")
    void testInvalidProductTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/optimized/receive")
                        .param("type", "furniture")
                        .param("count", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Unsupported product type")));
    }
}
