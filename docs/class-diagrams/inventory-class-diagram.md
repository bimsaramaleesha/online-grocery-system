# Member 2: Grocery Inventory & Customer Storefront Module
**Module:** Grocery Inventory & Items (`com.grocery.inventory` + `com.grocery.common`)  
**Agreed ID Prefix:** `ITM` | **URL Mapping:** `/inventory` & `/shop` | **Storage File:** `data/items.txt`

---

## 1. Class Diagram (Inventory + Common Modules)

```mermaid
classDiagram
    class Item {
        <<abstract>>
        -int LOW_STOCK_LIMIT$
        -String id
        -String name
        -String category
        -double price
        -int quantity
        -String imageUrl
        #Item(String, String, String, double, int)
        +getId() String
        +setName(String) void
        +setCategory(String) void
        +setPrice(double) void
        +setQuantity(int) void
        +isLowStock() boolean
        +isExpired() boolean
        +isExpiringSoon() boolean
        +toFileLine() String
        +getItemType()* String
        +getTypeLabel()* String
        +getSellingPrice()* double
        +getDetails()* String
        #getExtraFields()* String
    }

    class PerishableItem {
        +String TYPE$
        -int SOON_DAYS$
        -double SOON_DISCOUNT$
        -LocalDate expiryDate
        +PerishableItem(String, String, String, double, int, LocalDate)
        +getExpiryDate() LocalDate
        +setExpiryDate(LocalDate) void
        +isExpired() boolean
        +isExpiringSoon() boolean
        +getItemType() String
        +getTypeLabel() String
        +getSellingPrice() double
        +getDetails() String
        #getExtraFields() String
    }

    class NonPerishableItem {
        +String TYPE$
        -String brand
        -int shelfLifeMonths
        +NonPerishableItem(String, String, String, double, int, String)
        +NonPerishableItem(String, String, String, double, int, String, int)
        +getBrand() String
        +setBrand(String) void
        +getShelfLifeMonths() int
        +setShelfLifeMonths(int) void
        +getItemType() String
        +getTypeLabel() String
        +getSellingPrice() double
        +getDetails() String
        #getExtraFields() String
    }

    class ItemFactory {
        -ItemFactory()
        +create(String, String, String, String, Double, Integer, LocalDate, String, Integer)$ Item
        +fromFileLine(String)$ Item
    }

    class ItemRepository {
        <<interface>>
        +findAll() List~Item~
        +findById(String) Optional~Item~
        +save(Item) Item
        +deleteById(String) boolean
    }

    class FileItemRepository {
        -String fileName
        +FileItemRepository(String)
        -seedDefaultItems() void
        +findAll() List~Item~
        +findById(String) Optional~Item~
        +save(Item) Item
        +deleteById(String) boolean
        -writeAll(List~Item~) void
        -nextId(List~Item~) String
    }

    class InventoryService {
        -ItemRepository repository
        +InventoryService(ItemRepository)
        +search(String) List~Item~
        +search(String, String) List~Item~
        +summary() InventorySummary
        +getById(String) Item
        +addItem(ItemForm) Item
        +updateItem(String, ItemForm) Item
        +restockItem(String, int) Item
        +deleteExpiredItems() int
        +deleteItem(String) void
        +shopProducts(String, String, String) List~ProductView~
    }

    class InventoryController {
        -InventoryService service
        +InventoryController(InventoryService)
        +shop(String, String, String, Model) String
        +product(String, Model, RedirectAttributes) String
        +list(String, String, Model) String
        +showAddForm(Model) String
        +add(ItemForm, BindingResult, Model, RedirectAttributes) String
        +showEditForm(String, Model, RedirectAttributes) String
        +update(String, ItemForm, BindingResult, Model, RedirectAttributes) String
        +restock(String, int, RedirectAttributes) String
        +deleteExpired(RedirectAttributes) String
        +delete(String, RedirectAttributes) String
    }

    class FileHandler {
        -Path DATA_DIR$
        -FileHandler()
        +readLines(String)$ List~String~
        +appendLine(String, String)$ void
        +writeLines(String, List~String~)$ void
        +clean(String)$ String
    }

    class IdGenerator {
        -IdGenerator()
        +nextId(String, List~String~)$ String
    }

    Item <|-- PerishableItem : Inheritance (is-a)
    Item <|-- NonPerishableItem : Inheritance (is-a)
    ItemRepository <|.. FileItemRepository : Realization (implements)
    InventoryService *-- ItemRepository : Composition / Dependency Injection (has-a)
    InventoryController *-- InventoryService : Composition / Dependency Injection (has-a)
    FileItemRepository ..> ItemFactory : Dependency (uses)
    FileItemRepository ..> FileHandler : Dependency (uses)
    FileItemRepository ..> IdGenerator : Dependency (uses)
    InventoryService ..> ItemFactory : Dependency (uses)
```

---

## 2. Object-Oriented Programming (OOP) Implementation

### A. Encapsulation
- All attributes in `Item` (`id`, `name`, `category`, `price`, `quantity`, `imageUrl`), `PerishableItem` (`expiryDate`), and `NonPerishableItem` (`brand`, `shelfLifeMonths`) are declared `private`.
- Setters are marked `public final` and validate inputs before assigning values (throwing `IllegalArgumentException` if name/category/brand is blank, if price/quantity/shelfLifeMonths is negative, or if `expiryDate` is null).
- Information hiding is applied in `Item.isLowStock()` which checks `quantity <= LOW_STOCK_LIMIT` without exposing the private constant `LOW_STOCK_LIMIT = 10`.

### B. Inheritance
- `Item` is the abstract parent class holding common fields and shared methods (`isLowStock()`, `toFileLine()`, `clean()`).
- `PerishableItem extends Item` uses `super(id, name, category, price, quantity)` and adds `expiryDate`, `SOON_DAYS = 3`, and `SOON_DISCOUNT = 0.20`.
- `NonPerishableItem extends Item` uses `super(id, name, category, price, quantity)` and adds `brand` and `shelfLifeMonths`.

### C. Polymorphism
1. **Runtime Polymorphism (Method Overriding):**
   - `getSellingPrice()`: `PerishableItem` gives a **20% discount** (`price * 0.80`) when expiring within 3 days (`isExpiringSoon() == true`); `NonPerishableItem` always returns the regular `getPrice()`.
   - `getDetails()`: `PerishableItem` returns expiry status (`"EXPIRED on ..."`, `"Expires soon: ... (20% off)"`, or `"Expires on ..."`), whereas `NonPerishableItem` returns brand and shelf life (`"Brand: X, shelf life: Y months"` or `"Brand: X (No expiry)"`).
   - `isExpired()` / `isExpiringSoon()`: Default to `false` in `Item`, overridden with `LocalDate` calculations in `PerishableItem`.
   - `getExtraFields()`: Template method hook called inside `Item.toFileLine()` so each child formats its own extra columns.
2. **Compile-Time Polymorphism (Method Overloading):**
   - `InventoryService.search(String keyword)` vs `InventoryService.search(String keyword, String filter)`.
   - `NonPerishableItem` constructor overloading (6 parameters vs 7 parameters using `this(...)` chaining).

### D. Abstraction
- Abstract class `Item` cannot be instantiated directly (`new Item()` is a compile error) and declares 5 abstract methods (`getItemType`, `getTypeLabel`, `getSellingPrice`, `getDetails`, `getExtraFields`).
- Interface `ItemRepository` defines *what* data operations exist (`findAll`, `findById`, `save`, `deleteById`), hiding *how* `FileItemRepository` stores data in `data/items.txt`.
