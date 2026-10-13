# Inventory Management System — Backend Only

**Course project:** A REST API using **Java 25 + Spring Boot 4.1.1 + MongoDB**.
No frontend is included. API requests can be tested using Postman, VS Code REST Client, or curl.

## What the app does

- Create, read, update, and delete inventory products.
- Save product details (SKU, name, category, description, quantity, unit price, and low-stock threshold).
- Search by product name/SKU or filter by category.
- Add or subtract stock using an atomic MongoDB update (stock cannot become negative).
- List low-stock products and calculate an inventory summary.
- Export the product list as CSV using OpenCSV.
- Validate requests and return appropriate HTTP status codes.

## Prerequisites

1. **JDK 25** (`java -version` should show version 25).
2. MongoDB Community Server running locally on port `27017` (or MongoDB Atlas with a connection URI).
3. IntelliJ IDEA, Eclipse/STS, or VS Code with Java extensions (optional).
4. Postman (optional). Maven is installed automatically by the included Maven Wrapper if needed.

## Run on Windows

1. Extract this ZIP and open the `InventoryManagementSystem` folder.
2. Start your MongoDB service. MongoDB Compass is only a graphical client, **not** the database server.
3. Open PowerShell/Terminal in the folder.
4. Test the code: `./mvnw.cmd test`
5. Start it: `./mvnw.cmd spring-boot:run`
6. Base URL: `http://localhost:8080/api/products`

For MongoDB Atlas, define the `MONGODB_URI` environment variable. In PowerShell for the current terminal session:

```powershell
$env:MONGODB_URI="mongodb+srv://USERNAME:PASSWORD@HOST/inventory_db"
.\mvnw.cmd spring-boot:run
```

Do not commit usernames, passwords, or real connection URLs into source control.
The URI is configured through `spring.mongodb.uri` (the Spring Boot 4.x property).
MongoDB will create the `inventory_db` database and `products` collection after the first product is inserted.

## REST API routes

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/products` | Create product; HTTP 201 |
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Find one product |
| PUT | `/api/products/{id}` | Replace product fields |
| DELETE | `/api/products/{id}` | Delete product; HTTP 204 |
| PATCH | `/api/products/{id}/stock` | Add/reduce stock |
| GET | `/api/products/search?keyword=pen` | Search name or SKU |
| GET | `/api/products/category/Stationery` | Filter category |
| GET | `/api/products/low-stock` | Show low-stock products |
| GET | `/api/products/summary` | Inventory totals |
| GET | `/api/products/export/csv` | Download products CSV |

### 1. Create a product

Method: `POST` — `http://localhost:8080/api/products`

Header: `Content-Type: application/json`

```json
{
  "sku": "PEN-001",
  "name": "Blue Ball Pen",
  "category": "Stationery",
  "description": "Blue ink ballpoint pen",
  "quantity": 30,
  "unitPrice": 15.50,
  "lowStockThreshold": 5
}
```

Successful result: HTTP **201 Created**, a JSON product containing an `id`, and a `Location` header.
Copy the returned `id` into the requests below.

### 2. Fetch products

Method: `GET` — `http://localhost:8080/api/products`

### 3. Find one product

Method: `GET` — `http://localhost:8080/api/products/YOUR_PRODUCT_ID`

### 4. Update product

Method: `PUT` — `http://localhost:8080/api/products/YOUR_PRODUCT_ID`
Provide a **complete** JSON body with the same seven product fields shown for POST.

### 5. Receive stock or remove stock

Method: `PATCH` — `http://localhost:8080/api/products/YOUR_PRODUCT_ID/stock`

Stock in:
```json
{"change": 10}
```

Stock out:
```json
{"change": -3}
```

`change: 0` returns HTTP 400. Removing more stock than exists returns HTTP 409.

### 6. Delete

Method: `DELETE` — `http://localhost:8080/api/products/YOUR_PRODUCT_ID`

### 7. Other views

- `GET http://localhost:8080/api/products/low-stock`
- `GET http://localhost:8080/api/products/summary`
- `GET http://localhost:8080/api/products/search?keyword=pen`
- `GET http://localhost:8080/api/products/category/Stationery`
- `GET http://localhost:8080/api/products/export/csv`

Low-stock means `quantity <= lowStockThreshold`. Summary stock value is `quantity × unitPrice` summed over products.

## Error codes

- **400 Bad Request** — invalid fields or zero stock adjustment.
- **404 Not Found** — requested product ID does not exist.
- **409 Conflict** — duplicate SKU, insufficient stock, or stock limit reached.

## Sample project explanation for classroom demonstration

The Inventory Management System is a backend REST application built with Java Spring Boot and MongoDB. It uses the Model–Repository–Service–Controller architecture to manage inventory information. Users can add and edit products, search records, monitor low stock, update quantities, calculate stock value, and export records as CSV. APIs are tested using Postman, while MongoDB stores the data persistently.

## Folder overview

```text
InventoryManagementSystem/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/wrapper/maven-wrapper.properties
├── src/main/java/com/pgs/inventorymanagement/
│   ├── InventoryManagementApplication.java
│   ├── controller/ProductController.java
│   ├── dto/{ProductRequest,StockAdjustmentRequest,InventorySummary}.java
│   ├── exception/{...}.java
│   ├── model/Product.java
│   ├── repository/ProductRepository.java
│   └── service/{ProductService,CsvExportService}.java
├── src/main/resources/application.properties
├── src/test/java/com/pgs/inventorymanagement/service/{...}
└── postman/InventoryManagement.postman_collection.json
```

## Submission / demo evidence

Suggested screenshots: running application, POST successful product, GET all, PUT update, PATCH stock,
low-stock endpoint, summary endpoint, downloaded CSV, MongoDB Compass `inventory_db.products` collection.

## Notes and boundaries

- Intended for local class demonstration, not public Internet deployment (no login/authentication).
- One product has one SKU, normalized to uppercase; MongoDB enforces a unique index.
- PUT replaces all editable product fields; it is not a partial update.
- The quantity change via PATCH is atomic; general PUT and DELETE are basic CRUD operations.
- The inventory summary scans all products, suitable for a small classroom dataset.
- Automated unit tests are included, but integration tests need a running MongoDB instance.
