# Online Grocery Order Management System

SE1020 group project. A full-functional online grocery management system built with **Java, Spring Boot, Thymeleaf, HTML, CSS and JavaScript**. Data is stored in plain text files (no database).

Customers can browse groceries, add items to a cart, apply promo codes, place orders and leave reviews. Staff can manage inventory, suppliers, deliveries and customer complaints.

> **Team members:** read [CONTRIBUTING.md](CONTRIBUTING.md) before you write any code.

---

## Features (by module)

| # | Module | What it does | URL prefix | Owner |
|---|--------|--------------|------------|-------|
| 1 | User & Authentication | Registration, login, user accounts | `/users` | @MrHasit |
| 2 | Grocery Inventory & Items | Add, list, edit and delete grocery items and stock | `/items` | @bimsaramaleesha |
| 3 | Supplier & Vendor Logistics | Suppliers and delivery tracking | `/suppliers` | @Thilanjana01 |
| 4 | Cart & Order Processing | Shopping cart and orders | `/cart`, `/orders` | @Piumanjali K. K. |
| 5 | Discounts & Promo Codes | Create and apply promo codes | `/promos` | @Yashindi J.P.M |
| 6 | Reviews & Customer Complaints | Product reviews and complaints | `/reviews`, `/complaints` | @Pahan K. H. S. |

The full per-module conventions (packages, data files, ID prefixes) are in [CONTRIBUTING.md](CONTRIBUTING.md).

## Tech stack

| Part | Choice |
|------|--------|
| Language | Java 17 or newer |
| Framework | Spring Boot (Maven, with the Maven Wrapper) |
| Web pages | Thymeleaf templates + HTML / CSS / JavaScript |
| Data storage | Plain text files through the shared `FileHandler` |
| Collaboration | Git + GitHub (pull requests, CODEOWNERS, build check) |

## Getting started

**Requirements:** JDK 17 or newer, Git, and IntelliJ IDEA (recommended). You do not need to install Maven separately.

```bash
git clone https://github.com/bimsaramaleesha/online-grocery-system.git
cd online-grocery-system
git checkout develop
```

Run it from IntelliJ (open `GroceryApplication` and press the green play button), or from a terminal:

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080>. You should see the home page.

First-time Git and IntelliJ setup is explained step by step in [CONTRIBUTING.md](CONTRIBUTING.md#2-first-time-setup).

## Project structure

```
src/main/java/com/grocery/
    common/        shared helpers (FileHandler, IdGenerator, GlobalExceptionHandler)
    user/ inventory/ supplier/ order/ promo/ feedback/
        controller/   handles web requests
        model/        the classes (User, GroceryItem ...)
        service/      business logic
        repository/   reads and writes the .txt file using FileHandler
src/main/resources/
    templates/     HTML pages (Thymeleaf), one folder per module
    static/css     one CSS file per module + common.css
    static/js      one JS file per module + common.js
data/              live text files (created automatically, NOT in Git)
sample-data/       demo data
docs/              class diagrams and report
```

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Stable, demo-ready code. Only the team leader merges here. |
| `develop` | Where everyone's finished work comes together. Every pull request targets this branch. |
| `feature/<module>-<task>` | Short-lived working branches, e.g. `feature/inventory-add-item`. |

Full workflow and rules: [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Released under the [MIT License](LICENSE).
