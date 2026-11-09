# Data File Formats (6-Module Architecture Contract)

All files live in `data/` and are automatically created and seeded on first startup. One record per line, fields separated by `|`. User input is sanitized with `FileHandler.clean(...)` to prevent delimiter injection.

| File | Module | Prefix | Line Format |
|------|--------|--------|-------------|
| `users.txt` | Member 1 (`user`) | `U` | `TYPE\|id\|name\|email\|passwordHash\|createdDate\|extra...` (`CUSTOMER`: `phone\|address`; `ADMIN`: `department`) |
| `items.txt` | Member 2 (`inventory`) | `ITM` | `TYPE\|id\|name\|price\|quantity\|category\|extra\|supplierId\|unit` (`PERISHABLE`: `expiryDate`; `NON_PERISHABLE`: `warrantyMonths`) |
| `suppliers.txt` | Member 3 (`supplier`) | `SUP` | `TYPE\|id\|companyName\|contactNumber\|email\|supplyCategory\|extra1\|extra2\|extra3` (`LOCAL` or `WHOLESALE`) |
| `orders.txt` | Member 4 (`order`) | `ORD` | `orderId\|userId\|itemsSummary\|subtotal\|discountAmount\|netAmount\|status\|orderDate\|shippingAddress\|contactPhone\|paymentMethod\|promoCode` |
| `carts.txt` | Member 4 (`order`) | `CRT` | `cartId\|userId\|itemId\|quantity\|addedDate` |
| `promotions.txt` | Member 5 (`promo`) | `PRM` | `TYPE\|id\|name\|description\|couponCode\|discountValue\|minimumOrderAmount\|startDate\|expiryDate\|active` (`PERCENTAGE` or `FIXED`) |
| `feedbacks.txt` | Member 6 (`feedback`) | `REV` / `CMP` | `REVIEW\|id\|itemId\|userId\|userName\|rating\|title\|comment\|createdDate` OR `COMPLAINT\|id\|orderId\|userId\|userName\|category\|severity\|status\|comment\|resolutionNote\|createdDate` |

## Fault Isolation Between Modules

Modules do **not** import each other's Java classes or services. Whenever a module needs reference data from another module (e.g., `OrderService` checking `items.txt` or `promotions.txt`, `InventoryController` displaying product reviews from `feedbacks.txt`, or `UserController` showing order/inventory counts on `/admin`), it reads the text file directly via `FileHandler.readLines(...)` inside a `try/catch` block with safe fallback defaults. If any module or file is modified or temporarily down, all other modules continue operating normally.
