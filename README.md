# Online Grocery Order Management System

A full-functional online grocery management system built with **Java** and **Spring Boot** as a campus group project (SE1020).

Customers can browse groceries, add items to a cart, apply promo codes and place orders. Staff can manage inventory, suppliers and customer complaints.

> **Team members:** read [CONTRIBUTING.md](CONTRIBUTING.md) before you write any code.

---

## Tech stack

| Part | Choice |
|------|--------|
| Language | Java 17 (or newer) |
| Framework | Spring Boot (Maven) |
| Web pages | Thymeleaf templates + HTML/CSS/JS |
| Data storage | Text files via the shared `FileHandler` (see `sample-data/`) |
| Version control | Git + GitHub (Pull Requests, CODEOWNERS) |

## Modules and owners

Each member owns one module. Do not edit another member's module without talking to them first.

| # | Module | Owner (GitHub) |
|---|--------|----------------|
| 1 | User & Authentication | @MrHasit |
| 2 | Grocery Inventory & Items | @bimsaramaleesha |
| 3 | Supplier & Vendor Logistics | @Thilanjana01 |
| 4 | Cart & Order Processing | @Piumanjali K. K. |
| 5 | Discounts & Promo Codes | @Yashindi J.P.M |
| 6 | Reviews & Customer Complaints | @Pahan K. H. S. |

## Getting started

### 1. Requirements

- JDK 17 or newer (`java -version` to check)
- Git
- IntelliJ IDEA (recommended)

You do **not** need to install Maven separately. The project includes the Maven Wrapper.

### 2. Clone the repository

```bash
git clone https://github.com/bimsaramaleesha/online-grocery-system.git
cd online-grocery-system
git checkout develop
```

### 3. Run the application

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw spring-boot:run
```

Or open the project in IntelliJ and run the `GroceryApplication` class.

Then open <http://localhost:8080> in your browser.

### 4. Run the build check locally

Run this before opening a Pull Request. It is the same check GitHub runs.

```bash
./mvnw clean verify        # Windows: mvnw.cmd clean verify
```

## Project structure

```
online-grocery-system/
├── docs/                 Project documents and diagrams
├── sample-data/          Sample data files used by FileHandler
├── src/main/java/com/grocery/
│   ├── GroceryApplication.java
│   ├── common/           Shared helpers (FileHandler, IdGenerator, GlobalExceptionHandler)
│   └── <module>/         One package per module (model, repository, service, controller)
├── src/main/resources/
│   ├── templates/        Thymeleaf pages (layout.html + one folder per module)
│   └── static/           CSS and JavaScript (one file per module)
├── CONTRIBUTING.md       How we work together (branches, commits, PRs)
└── pom.xml
```

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Stable, working versions only. Updated at milestones and for the final submission. |
| `develop` | Where all finished work comes together. Every PR targets this branch. |
| `feature/*` | Short-lived branches where each member does their work. |

Full rules are in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Released under the [MIT License](LICENSE).
