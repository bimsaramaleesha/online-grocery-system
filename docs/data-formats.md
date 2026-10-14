# Data file formats (the contract between modules)

All files live in `data/` (not in Git). One record per line, fields separated by `|`. No comment lines (the app rewrites whole files). Text typed by users goes through `FileHandler.clean(...)` first.

| File | Module | Prefix | Line format |
|------|--------|--------|-------------|
| `users.txt` | user | `U` | decided by the user owner, first field `type` if you have child classes (e.g. `CUSTOMER` / `ADMIN`) then `id` |
| `items.txt` | inventory | `ITM` | `TYPE\|id\|name\|category\|price\|quantity\|extra...` |
| `suppliers.txt` | supplier | `SUP` | `TYPE\|id\|companyName\|contact\|email\|category\|extra...` (see `Supplier.toFileString()`) |
| `carts.txt` | order | `CRT` | `cartId\|userId\|itemId\|quantity\|addedDate` |
| `orders.txt` | order | `ORD` | `orderId\|userId\|itemsSummary\|subtotal\|discountAmount\|netAmount\|status\|orderDate\|shippingAddress\|contactPhone\|paymentMethod\|promoCode` |
| `promos.txt` | promo | `PRM` | `id\|code\|type\|value\|minSpend\|...your own extra fields` |
| `reviews.txt` / `complaints.txt` | feedback | `REV` / `CMP` | start with `id`, then `itemId` (review) or `orderId` (complaint) |

## items.txt (inventory)

```
PERISHABLE|ITM001|Fresh Milk 1L|Dairy|420.0|25|2026-10-25            extra = expiryDate (yyyy-MM-dd)
NON_PERISHABLE|ITM006|Basmati Rice 5kg|Grains|1850.0|40|Araliya|24   extra = brand|shelfLifeMonths
```

IDs have no dash (`ITM001`), always made by `IdGenerator.nextId`.

## promos.txt (promo, read by the order module's discount factory)

The first five columns are fixed because `DiscountStrategyFactory` already reads them:

```
PRM001|WELCOME10|PERCENTAGE|10.0|0.0|2026-01-01|2026-12-31|true
PRM002|SAVE200|FIXED|200.0|1000.0|2026-01-01|2026-12-31|true
```

| Column | Meaning |
|--------|---------|
| 1 `id` | `PRM001` ... |
| 2 `code` | the promo code a customer types (case-insensitive) |
| 3 `type` | `PERCENTAGE` or `FIXED` |
| 4 `value` | percent (10.0 = 10%) or money amount |
| 5 `minSpend` | minimum order total (0.0 = none) |
| 6+ | free for the promo owner: start date, expiry date, active flag ... |

## Links between modules (by ID only)

- cart / order line -> `itemId` (an `ITM...` ID from `items.txt`)
- order -> `userId` (a `U...` ID), `promoCode` (a code from `promos.txt`)
- review -> `itemId`, `userId`; complaint -> `orderId`, `userId`
- supplier restock -> `itemId`
