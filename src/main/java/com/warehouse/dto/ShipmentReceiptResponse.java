package com.warehouse.dto;

import java.util.List;

/**
 * Data Transfer Object representing the outcome and memory telemetry of
 * receiving a warehouse shipment.
 */
public class ShipmentReceiptResponse {

    private String status;
    private String implementation;
    private String productType;
    private int itemCount;
    private int catalogInstancesCreated;
    private double heavyPayloadSizePerItemMB;
    private long usedMemoryBytes;
    private double usedMemoryMB;
    private double initialMemoryMB;
    private double finalMemoryMB;
    private long executionTimeMs;
    private String message;
    private List<String> sampleItems;

    public ShipmentReceiptResponse() {
    }

    public ShipmentReceiptResponse(String status, String implementation, String productType,
                                   int itemCount, int catalogInstancesCreated,
                                   double heavyPayloadSizePerItemMB, long usedMemoryBytes,
                                   double usedMemoryMB, double initialMemoryMB,
                                   double finalMemoryMB, long executionTimeMs,
                                   String message, List<String> sampleItems) {
        this.status = status;
        this.implementation = implementation;
        this.productType = productType;
        this.itemCount = itemCount;
        this.catalogInstancesCreated = catalogInstancesCreated;
        this.heavyPayloadSizePerItemMB = heavyPayloadSizePerItemMB;
        this.usedMemoryBytes = usedMemoryBytes;
        this.usedMemoryMB = usedMemoryMB;
        this.initialMemoryMB = initialMemoryMB;
        this.finalMemoryMB = finalMemoryMB;
        this.executionTimeMs = executionTimeMs;
        this.message = message;
        this.sampleItems = sampleItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getCatalogInstancesCreated() {
        return catalogInstancesCreated;
    }

    public void setCatalogInstancesCreated(int catalogInstancesCreated) {
        this.catalogInstancesCreated = catalogInstancesCreated;
    }

    public double getHeavyPayloadSizePerItemMB() {
        return heavyPayloadSizePerItemMB;
    }

    public void setHeavyPayloadSizePerItemMB(double heavyPayloadSizePerItemMB) {
        this.heavyPayloadSizePerItemMB = heavyPayloadSizePerItemMB;
    }

    public long getUsedMemoryBytes() {
        return usedMemoryBytes;
    }

    public void setUsedMemoryBytes(long usedMemoryBytes) {
        this.usedMemoryBytes = usedMemoryBytes;
    }

    public double getUsedMemoryMB() {
        return usedMemoryMB;
    }

    public void setUsedMemoryMB(double usedMemoryMB) {
        this.usedMemoryMB = usedMemoryMB;
    }

    public double getInitialMemoryMB() {
        return initialMemoryMB;
    }

    public void setInitialMemoryMB(double initialMemoryMB) {
        this.initialMemoryMB = initialMemoryMB;
    }

    public double getFinalMemoryMB() {
        return finalMemoryMB;
    }

    public void setFinalMemoryMB(double finalMemoryMB) {
        this.finalMemoryMB = finalMemoryMB;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSampleItems() {
        return sampleItems;
    }

    public void setSampleItems(List<String> sampleItems) {
        this.sampleItems = sampleItems;
    }
}
