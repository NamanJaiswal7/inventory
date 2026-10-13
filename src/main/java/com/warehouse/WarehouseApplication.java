package com.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Warehouse Inventory Management Application.
 *
 * Demonstrates:
 * 1. Flyweight Pattern vs. Naive Memory Duplication
 * 2. Factory Method Pattern vs. Tightly-Coupled Conditionals
 */
@SpringBootApplication
public class WarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
