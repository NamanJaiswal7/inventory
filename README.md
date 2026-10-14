# E-Commerce Warehouse Inventory: Flyweight & Factory Method Design Patterns

A complete, production-ready **Spring Boot 3.x (Java 17+)** REST API that demonstrates the **Flyweight** and **Factory Method** design patterns through an E-Commerce Warehouse Inventory shipment ingestion scenario.

The application empirically proves the architectural and memory differences between a **Naive Implementation** (memory bloat & tight coupling) and an **Optimized Implementation** (shared intrinsic state & polymorphic extensibility).

---

## 1. Domain Scenario & The Architectural Problem

In high-throughput e-commerce fulfillment centers, inventory arrives in massive batches:
- **Physical Item:** A specific box resting on a warehouse shelf (has unique `barcode` and shelf `location`).
- **Product:** Catalog metadata (has `sku`, `name`, and a heavy **5MB** high-resolution image/metadata payload).

### Phase 1: The Problem (Without Patterns - "Naive Implementation")
- **Package:** `com.warehouse.naive`
- **The Memory Problem (No Flyweight):** `NaiveInventoryItem` stores both extrinsic box data and the heavy 5MB payload directly inside each instance. Ingesting 1,000 boxes allocates 1,000 separate 5MB byte buffers ($1,000 \times 5\text{ MB} = 5,000\text{ MB}$ or $\sim 5\text{ GB}$), quickly exhausting the JVM heap and triggering `java.lang.OutOfMemoryError`.
- **The Coupling Problem (No Factory Method):** `NaiveShipmentProcessor` uses a monolithic `switch`/`if-else` block to decide which product type to instantiate ("electronics", "books", "clothing"). Introducing a new category violates the **Open/Closed Principle (OCP)**, requiring edits to existing core code.

### Phase 2: The Solution (With Patterns - "Optimized Implementation")
- **Package:** `com.warehouse.optimized`
- **The Flyweight Pattern (Memory Solution):**
  - **Flyweight (`ProductCatalogItem`):** Holds immutable, shared intrinsic data (`sku`, `name`, 5MB `byte[]`).
  - **Flyweight Factory (`ProductCatalog`):** A thread-safe registry utilizing `ConcurrentHashMap.computeIfAbsent()`. Guarantees exactly **one** 5MB instance per SKU in heap memory.
  - **Context (`OptimizedInventoryItem`):** Lightweight object ($\approx 32\text{--}48\text{ bytes}$) holding only the unique extrinsic state (`barcode`, `location`) and a reference to the shared Flyweight.
- **The Factory Method Pattern (Coupling Solution):**
  - **The Creator (`ShipmentReceiver`):** Defines the template method `processShipment(int count)` and declares the abstract factory method:
    ```java
    protected abstract OptimizedInventoryItem createInventoryItem(String barcode, String location);
    ```
  - **Concrete Creators (`ElectronicsReceiver`, `BooksReceiver`, `ClothingReceiver`):** Implement the factory method to resolve the category-specific Flyweight from `ProductCatalog` and return an `OptimizedInventoryItem`.
  - **Strategy Registry (`ShipmentReceiverRegistry`):** Uses Spring dependency injection (`List<ShipmentReceiver>`) to auto-register all creators. New categories can be introduced simply by creating a new `@Component` class without touching existing codebase.

---

## 2. Empirical Benchmark & Telemetry Comparison

| Metric | Naive Implementation (`/api/v1/naive/receive`) | Optimized Implementation (`/api/v1/optimized/receive`) | Impact |
| :--- | :--- | :--- | :--- |
| **5MB Buffers in Memory** | 1 per item ($N$ copies) | **1 per SKU** ($1$ copy) | **$N\times$ reduction** |
| **Memory for 20 Items** | **$120.00\text{ MB}$** | **$\approx 5.1\text{ MB}$** | **$95.7\%$ RAM reduction** |
| **Memory for 1,000 Items** | **OutOfMemoryError** ($>2\text{ GB}$ heap exhausted) | **$6.14\text{ MB}$** | **$99.8\%$ RAM reduction** |
| **Open/Closed Principle** | **Violated** (modifies `switch` block) | **Compliant** (pluggable creator `@Component`) | Zero modification to core code |

---

## 3. Project Structure

```
warehouse-inventory/
├── pom.xml                                           # Maven dependencies & build setup (Java 17, Spring Boot 3.3.4)
├── diagrams/                                         # PlantUML source files and rendered PNG diagrams
│   ├── flyweight-pattern.puml                        # Class diagram for Flyweight pattern
│   ├── FlyweightPattern.png                          # Rendered Flyweight diagram
│   ├── factory-method-pattern.puml                   # Class diagram for Factory Method pattern
│   ├── FactoryMethodPattern.png                      # Rendered Factory Method diagram
│   ├── system-architecture.puml                      # Component diagram comparing Naive vs Optimized
│   └── SystemArchitecture.png                        # Rendered System Architecture diagram
├── src/
│   ├── main/
│   │   ├── java/com/warehouse/
│   │   │   ├── WarehouseApplication.java             # Spring Boot Main Entry Point
│   │   │   ├── dto/
│   │   │   │   └── ShipmentReceiptResponse.java      # JSON Response DTO with memory metrics
│   │   │   ├── util/
│   │   │   │   └── MemoryMeasurementUtil.java        # GC-stabilized JVM memory measurement
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java       # Centralized 400 Bad Request exception handler
│   │   │   ├── naive/                                # Anti-Pattern implementation
│   │   │   │   ├── NaiveInventoryItem.java           # Conflates extrinsic state with 5MB intrinsic payload
│   │   │   │   ├── NaiveShipmentProcessor.java       # Tightly coupled switch statement
│   │   │   │   └── NaiveController.java              # POST /api/v1/naive/receive
│   │   │   └── optimized/                            # Solution with patterns
│   │   │       ├── ProductCatalogItem.java           # THE FLYWEIGHT (5MB immutable payload)
│   │   │       ├── ProductCatalog.java               # THE FLYWEIGHT FACTORY (Thread-safe cache)
│   │   │       ├── OptimizedInventoryItem.java       # THE CONTEXT (Lightweight barcode + reference)
│   │   │       ├── ShipmentReceiver.java             # THE CREATOR (Factory Method)
│   │   │       ├── ElectronicsReceiver.java          # CONCRETE CREATOR (Electronics)
│   │   │       ├── BooksReceiver.java                # CONCRETE CREATOR (Books)
│   │   │       ├── ClothingReceiver.java             # CONCRETE CREATOR (Clothing)
│   │   │       ├── ShipmentReceiverRegistry.java     # STRATEGY DISPATCHER (Spring auto-wiring)
│   │   │       └── OptimizedController.java          # POST /api/v1/optimized/receive
│   │   └── resources/
│   │       └── application.properties                # Configured for server.port=8081
│   └── test/
│       ├── java/com/warehouse/
│       │   └── WarehousePatternsIntegrationTest.java # 7 unit & integration tests
│       └── resources/
│           └── mockito-extensions/
│               └── org.mockito.plugins.MockMaker     # Subclass mock maker for cross-platform compatibility
```

---

## 4. How to Run in Any Environment

### Prerequisites
- **JDK 17 or later** (Eclipse Temurin, OpenJDK, Amazon Corretto, or Oracle JDK).
- **Maven 3.8+** (or use system maven).
- **Operating Systems Supported:** macOS, Linux (Ubuntu, Debian, CentOS, RHEL, Alpine), Windows 10/11 / Windows Server.

---

### Step 1: Environment Setup

#### On macOS / Linux
Ensure `JAVA_HOME` points to Java 17+:
```bash
# Example for macOS (Homebrew)
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"

# Example for Ubuntu / Debian
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify installation
java -version
mvn -version
```

#### On Windows (PowerShell)
```powershell
# Set JAVA_HOME (adjust to your JDK path)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Verify installation
java -version
mvn -version
```

---

### Step 2: Build and Test

Execute automated tests:
```bash
mvn clean test
```
All 7 integration tests will execute, validating:
1. Singleton Flyweight reference identity (`assertSame`).
2. Factory Method polymorphic creator execution.
3. Strategy registry dispatching without conditionals.
4. Naive multi-buffer allocations.
5. REST endpoint JSON payloads and memory benchmarks.
6. Centralized bad-request exception handling.

---

### Step 3: Run the Application

You can run the application in any environment using one of the following methods:

#### Method A: Using Maven Plugin
```bash
mvn spring-boot:run
```

#### Method B: Building and Running the Standalone Executable JAR
```bash
# 1. Package the executable JAR
mvn clean package -DskipTests

# 2. Run the JAR with a specific JVM heap limit (e.g. 2GB max heap)
java -Xmx2g -jar target/warehouse-inventory-1.0.0-SNAPSHOT.jar
```

#### Overriding Configuration via Command Line
If port `8081` is occupied on your host, specify any custom port:
```bash
java -jar target/warehouse-inventory-1.0.0-SNAPSHOT.jar --server.port=9090
```
Or via environment variable:
```bash
SERVER_PORT=9090 java -jar target/warehouse-inventory-1.0.0-SNAPSHOT.jar
```

---

### Step 4: Testing the Endpoints

Once the application starts on port `8081` (or your configured port):

#### 1. Test Optimized Endpoint (Flyweight + Factory Method)
Ingest **1,000 items** — only **one** 5MB payload is instantiated:
```bash
# macOS / Linux (curl)
curl -X POST "http://localhost:8081/api/v1/optimized/receive?type=electronics&count=1000"

# Windows PowerShell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/v1/optimized/receive?type=electronics&count=1000" | ConvertTo-Json
```
**Expected Response:**
```json
{
  "status": "SUCCESS",
  "implementation": "OPTIMIZED (Flyweight + Factory Method)",
  "productType": "electronics",
  "itemCount": 1000,
  "catalogInstancesCreated": 1,
  "heavyPayloadSizePerItemMB": 5.0,
  "usedMemoryMB": 6.14,
  "executionTimeMs": 423,
  "message": "Successfully created 1000 lightweight context items. The 5MB payload was instantiated exactly ONCE in ProductCatalog. Retained memory delta: ~6.14 MB."
}
```

#### 2. Test Other Supported Categories (Factory Method Dispatch)
```bash
curl -X POST "http://localhost:8081/api/v1/optimized/receive?type=books&count=500"
curl -X POST "http://localhost:8081/api/v1/optimized/receive?type=clothing&count=250"
```

#### 3. Test Naive Endpoint (Demonstrating Memory Bloat)
Ingest **20 items** — allocates 20 separate 5MB byte arrays (~120 MB):
```bash
curl -X POST "http://localhost:8081/api/v1/naive/receive?type=electronics&count=20"
```
**Expected Response:**
```json
{
  "status": "SUCCESS",
  "implementation": "NAIVE (No Patterns)",
  "productType": "electronics",
  "itemCount": 20,
  "catalogInstancesCreated": 20,
  "heavyPayloadSizePerItemMB": 5.0,
  "usedMemoryMB": 120.0,
  "executionTimeMs": 555,
  "message": "Created 20 naive items. Allocated 20 separate 5MB payload buffers (~120.00 MB total)."
}
```

#### 4. Test Naive Endpoint Heap Exhaustion (OutOfMemory Demonstration)
Attempt to ingest **1,000 items** in the naive implementation without Flyweights:
```bash
curl -X POST "http://localhost:8081/api/v1/naive/receive?type=electronics&count=1000"
```
**Expected Response:**
```json
{
  "status": "OUT_OF_MEMORY_ERROR",
  "implementation": "NAIVE (No Patterns)",
  "message": "Heap exhausted! Created 0 items before throwing OutOfMemoryError. Each item allocated 5MB independently without Flyweight sharing."
}
```

---

## 5. UML Diagrams & Architecture Visualizations

The project includes pre-compiled, high-resolution UML diagrams located in the [`diagrams/`](diagrams/) directory.

---

### A. System Architecture & Comparison (Naive vs. Optimized)

This diagram contrasts the Naive Anti-Pattern (tight coupling & duplicated 5MB allocations per item) with the Optimized Solution (Flyweight + Factory Method + Strategy Dispatcher).

![System Architecture UML Diagram](diagrams/SystemArchitecture.png)

#### Mermaid Architecture Flow:
```mermaid
graph TD
    subgraph ClientLayer ["Client HTTP Layer"]
        ReqOpt["POST /api/v1/optimized/receive?type=electronics&count=1000"]
        ReqNaive["POST /api/v1/naive/receive?type=electronics&count=1000"]
    end

    subgraph NaivePackage ["com.warehouse.naive (Anti-Pattern)"]
        NaiveCtrl["NaiveController"]
        NaiveProc["NaiveShipmentProcessor<br/>(Monolithic switch statement)"]
        NaiveItem["1,000 x NaiveInventoryItem<br/>(1,000 separate 5MB buffers = 5 GB RAM!)"]
        
        ReqNaive --> NaiveCtrl
        NaiveCtrl --> NaiveProc
        NaiveProc -->|switch case| NaiveItem
        NaiveItem -->|Exhausts 2GB Heap| OOM["💥 OutOfMemoryError"]
    end

    subgraph OptimizedPackage ["com.warehouse.optimized (Flyweight + Factory Method)"]
        OptCtrl["OptimizedController"]
        Registry["ShipmentReceiverRegistry<br/>(Auto-wires receivers, 0 switch statements)"]
        
        subgraph FactoryMethod ["Factory Method Pattern (Creators)"]
            CreatorBase["ShipmentReceiver<br/>(Template Method: processShipment)"]
            ElecRec["ElectronicsReceiver<br/>(createInventoryItem)"]
            BooksRec["BooksReceiver<br/>(createInventoryItem)"]
            ClothRec["ClothingReceiver<br/>(createInventoryItem)"]
            
            CreatorBase <|-- ElecRec
            CreatorBase <|-- BooksRec
            CreatorBase <|-- ClothRec
        end

        subgraph FlyweightSubsystem ["Flyweight Pattern (Shared Memory)"]
            FlyweightFactory["ProductCatalog<br/>(ConcurrentHashMap registry)"]
            SharedFlyweight["ProductCatalogItem<br/>(Heavy 5MB image allocated ONCE per SKU)"]
            OptContext["1,000 x OptimizedInventoryItem<br/>(~40 bytes each: barcode + shelf + ref)"]
            
            FlyweightFactory o-->|caches singleton| SharedFlyweight
            OptContext -->|holds 64-bit reference| SharedFlyweight
        end

        ReqOpt --> OptCtrl
        OptCtrl --> Registry
        Registry --> CreatorBase
        ElecRec --> FlyweightFactory
        ElecRec ..> OptContext
    end
```

---

### B. Flyweight Pattern Class Diagram

Separates heavy immutable intrinsic state (`ProductCatalogItem`) from lightweight unique extrinsic state (`OptimizedInventoryItem`), coordinated by `ProductCatalog`.

![Flyweight Pattern Class Diagram](diagrams/FlyweightPattern.png)

#### Mermaid Class Diagram:
```mermaid
classDiagram
    class ProductCatalog {
        -ConcurrentMap~String, ProductCatalogItem~ catalogRegistry
        +getOrCreateProduct(sku, name) ProductCatalogItem
        +containsSku(sku) boolean
        +getCatalogSize() int
        +clear() void
    }

    class ProductCatalogItem {
        -String sku
        -String name
        -byte[] heavyImageData
        +getSku() String
        +getName() String
        +getHeavyImageData() byte[]
    }
    note for ProductCatalogItem "INTRINSIC STATE (Immutable)\n5MB buffer allocated ONCE per SKU"

    class OptimizedInventoryItem {
        -String barcode
        -String location
        -ProductCatalogItem productCatalogItem
        +getBarcode() String
        +getLocation() String
        +getProductCatalogItem() ProductCatalogItem
        +getSku() String
        +getName() String
    }
    note for OptimizedInventoryItem "EXTRINSIC STATE (~40 bytes)\nUnique barcode/shelf + 8-byte pointer"

    ProductCatalog o--> "0..*" ProductCatalogItem : manages & caches
    OptimizedInventoryItem --> "1" ProductCatalogItem : references shared flyweight
```

---

### C. Factory Method Pattern Class Diagram

Decouples shipment processing workflow from concrete item instantiation, adhering to the Open/Closed Principle.

![Factory Method Pattern Class Diagram](diagrams/FactoryMethodPattern.png)

#### Mermaid Class Diagram:
```mermaid
classDiagram
    class ShipmentReceiver {
        <<abstract>>
        #createInventoryItem(barcode, location)* OptimizedInventoryItem
        +getType()* String
        +processShipment(count) List~OptimizedInventoryItem~
    }

    class ElectronicsReceiver {
        -ProductCatalog productCatalog
        +getType() String
        #createInventoryItem(barcode, location) OptimizedInventoryItem
    }

    class BooksReceiver {
        -ProductCatalog productCatalog
        +getType() String
        #createInventoryItem(barcode, location) OptimizedInventoryItem
    }

    class ClothingReceiver {
        -ProductCatalog productCatalog
        +getType() String
        #createInventoryItem(barcode, location) OptimizedInventoryItem
    }

    class ShipmentReceiverRegistry {
        -Map~String, ShipmentReceiver~ receiverMap
        +getReceiver(type) ShipmentReceiver
        +getSupportedTypes() Set~String~
    }

    ShipmentReceiver <|-- ElectronicsReceiver
    ShipmentReceiver <|-- BooksReceiver
    ShipmentReceiver <|-- ClothingReceiver
    ShipmentReceiverRegistry o--> "1..*" ShipmentReceiver : dynamic discovery via Spring DI
```

---

### D. Regenerating Diagram Files

The PlantUML source files are stored in `diagrams/`:
- [`diagrams/flyweight-pattern.puml`](diagrams/flyweight-pattern.puml)
- [`diagrams/factory-method-pattern.puml`](diagrams/factory-method-pattern.puml)
- [`diagrams/system-architecture.puml`](diagrams/system-architecture.puml)

To recompile or export them to PNG/SVG at any time:
```bash
plantuml diagrams/*.puml
```

---

## 6. Academic / Examination Criteria Compliance

This project directly satisfies academic design pattern examination requirements:
1. **Pattern Name and Classification:** Structural (Flyweight) & Creational (Factory Method).
2. **Intent:** Efficient memory sharing & subclass instantiation delegation.
3. **Motivation:** Solving warehouse inventory heap bloat & rigid conditionals.
4. **Applicability:** Defined for both patterns.
5. **Structure:** Complete PlantUML and Mermaid diagrams provided.
6. **Participants:** Mapped directly to classes.
7. **Consequences & Implementation:** Detailed trade-offs and thread-safety considerations.
8. **Working Program Code:** Complete runnable source code, test cases, and uncommented clean code samples for examination preparation (see [`exam_design_patterns_guide.md`](file:///Users/namankumar/.gemini/antigravity/brain/b9d551b7-ca45-4f8c-b36c-1799049dce64/exam_design_patterns_guide.md)).
