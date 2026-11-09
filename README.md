# Online Grocery Order Management System

SE1020 Object-Oriented Programming group project. A full-functional online grocery management system built with **Java, Spring Boot, Thymeleaf, HTML, CSS and JavaScript**. Data is stored in plain text files (`data/*.txt`) via `FileHandler` and automatically seeded on first launch.

Customers can browse groceries, add items to a cart, apply promo codes, place orders and leave reviews or complaints. Administrators can manage users, inventory, suppliers, orders, promotions and customer complaints.

> **Team members:** read [CONTRIBUTING.md](CONTRIBUTING.md) before you write any code, then [docs/project-walkthrough.md](docs/project-walkthrough.md) (how everything works) and [docs/data-formats.md](docs/data-formats.md) (file formats shared between modules).

---

## System Architecture (6 Independent Modules)

Each module is self-contained and loosely coupled — modules communicate through `data/*.txt` files using fault-tolerant `FileHandler` readers so modifying or testing one module never breaks the others.

| # | Module | Package | Data File | URL Prefix |
|---|--------|---------|-----------|------------|
| 1 | User & Authentication | `com.grocery.user` | `users.txt` | `/login`, `/register`, `/users`, `/admin` |
| 2 | Grocery Inventory & Items | `com.grocery.inventory` | `items.txt` | `/shop`, `/inventory` |
| 3 | Supplier & Vendor Logistics | `com.grocery.supplier` | `suppliers.txt` | `/suppliers` |
| 4 | Cart & Order Processing | `com.grocery.order` | `orders.txt` (`carts.txt`) | `/cart`, `/orders` |
| 5 | Discounts & Promo Codes | `com.grocery.promo` | `promotions.txt` | `/promos` |
| 6 | Reviews & Customer Complaints | `com.grocery.feedback` | `feedbacks.txt` | `/reviews`, `/complaints` |

## Tech Stack

| Part | Choice |
|------|--------|
| Language | Java 17 or newer |
| Framework | Spring Boot (Maven, with the Maven Wrapper) |
| Web pages | Thymeleaf templates + Bootstrap 5 + HTML / CSS / JavaScript |
| Data storage | Plain text files (`data/*.txt`) through the shared `FileHandler` |
| Security | PBKDF2-HMAC-SHA256 salted password hashing, session fixation protection, role-based access (`SessionAuth`), IDOR prevention |

## Getting Started (Deployable Out-of-the-Box)

```bash
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080>. Default accounts and sample items/suppliers/promotions/feedbacks are automatically seeded into `data/` on first run:
- **Admin:** `admin@grocery.lk` / `Admin@123`
- **Customer:** `customer@grocery.lk` / `Customer@123`

## Project Structure

```
src/main/java/com/grocery/
    common/        shared helpers (FileHandler, IdGenerator, GlobalExceptionHandler, WebConfig)
    user/          Member 1: User, Customer, Admin, UserService, UserController, SessionAuth
    inventory/     Member 2: Item, PerishableItem, NonPerishableItem, InventoryService, InventoryController
    supplier/      Member 3: Supplier, LocalFarmerSupplier, WholesaleDistributor, SupplierService, SupplierController
    order/         Member 4: Order (CashOrder, CardOrder), CartItem, OrderService, OrderController
    promo/         Member 5: Promotion (PercentagePromotion, FixedPromotion), PromotionService, PromotionController
    feedback/      Member 6: Feedback, Review, Complaint, FeedbackService, FeedbackController
src/main/resources/
    templates/     HTML pages (Thymeleaf), one folder per module (user, inventory, supplier, order, promo, feedback)
    static/css     one CSS file per module + common.css
    static/js      one JS file per module + common.js
data/              live text files (created & seeded automatically on startup)
docs/              architecture & data format guides
```

## License

Released under the [MIT License](LICENSE).
