# Module Pattern (copy this for your own module)

Reference module: **Inventory** (Member 2). Read it in this order:

| # | File | Job (one sentence) |
|---|------|--------------------|
| 1 | `model/Item.java` | Abstract parent: private fields, validating setters, abstract methods |
| 2 | `model/PerishableItem.java`, `NonPerishableItem.java` | Children: extra fields + their own version of the abstract methods |
| 3 | `model/ItemFactory.java` | Builds the correct child from form values or from a file line |
| 4 | `dto/ItemForm.java` | Plain object that carries the HTML form values |
| 5 | `repository/ItemRepository.java` | Interface: WHAT can be done (findAll, findById, save, deleteById) |
| 6 | `repository/FileItemRepository.java` | HOW: read/write `data/<module>.txt` through `FileHandler` |
| 7 | `service/InventoryService.java` | Rules + search; the controller only talks to this |
| 8 | `controller/InventoryController.java` | One method per URL; returns a page name or a redirect |
| 9 | `templates/inventory/list.html`, `form.html` | Pages (form.html is shared by Add and Edit) |
| 10 | `static/css/inventory.css`, `static/js/inventory.js` | Module styling and small UI helpers |

## Request flow
Browser -> Controller -> Service -> Repository -> FileHandler -> `data/items.txt`
and the answer travels back the same way.

## URL pattern (replace `inventory` with your module)
| Action | Method + URL |
|--------|--------------|
| List/search | GET `/inventory?q=` |
| Add | GET `/inventory/new`, POST `/inventory/new` |
| Edit | GET `/inventory/{id}/edit`, POST `/inventory/{id}/edit` |
| Delete | POST `/inventory/{id}/delete` |

## OOP checklist (the viva focuses on this)
- Encapsulation: private fields + validating setters (`Item`)
- Abstraction: abstract class `Item`, interface `ItemRepository`
- Inheritance: `PerishableItem`/`NonPerishableItem` extend `Item`
- Polymorphism: `getSellingPrice()`, `getDetails()`, `toFileLine()` behave differently per child
- Information hiding: `LOW_STOCK_LIMIT` is private, others only call `isLowStock()`

## File format rules
- One record per line, fields separated by `|`, first field = type code.
- Do NOT put comment lines in data files: the app rewrites the whole file on save.
- `data/items.txt` format: `TYPE|id|name|category|price|quantity|extra...`
  - PERISHABLE extra: `expiryDate` (yyyy-MM-dd)
  - NON_PERISHABLE extra: `brand|shelfLifeMonths`
- Each module owns its own file (`users.txt`, `suppliers.txt`, `orders.txt`, `promos.txt`, `reviews.txt`).

## Rules for your module
1. Only edit files inside your own module folders (CODEOWNERS enforces review).
2. Need something from another module? Ask its owner; don't edit their files.
3. Name your files the same way: `XxxRepository`, `FileXxxRepository`, `XxxService`, `XxxController`, `xxx.css`, `xxx.js`.
4. Minimum for marks: 3 CRUD operations, 3 pages, inheritance + polymorphism, file handling.
