# Project Outline for Teacher

**Title:** Inventory Management System (Backend)

**Problem statement:** Small shops and departments sometimes maintain stock on paper or disconnected spreadsheets, making it hard to track product quantities and identify shortages.

**Objective:** Develop a REST API to store product details, track quantities, and retrieve useful stock information.

**Technologies:** Java 25, Spring Boot 4.1.1, Spring MVC, Spring Data MongoDB, MongoDB, Maven, OpenCSV, Postman.

**Main modules:**
1. Product registration and modification (CRUD)
2. Product searching/filtering
3. Stock adjustments (addition/subtraction)
4. Low-stock list and inventory summary
5. CSV export

**Collection:** `products` inside database `inventory_db`.

**Product fields:** `id`, `sku` (unique), `name`, `category`, `description`, `quantity`, `unitPrice`, `lowStockThreshold`, `createdAt`, `updatedAt`.

**Architecture:** Controller -> Service -> Repository -> MongoDB; CSV Export Service handles export.

**Scope:** Backend REST API only. Frontend, user accounts, suppliers, billing and invoices are excluded.

**Testing method:** Unit tests via Maven, functional REST requests through Postman, collection inspection in MongoDB Compass.
