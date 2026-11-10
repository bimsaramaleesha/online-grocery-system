# Complete Project UML Class Diagrams (Full System + All 6 Member Zoomed-In Diagrams)

> **Print Instructions:** Open [printable-class-diagram.html](printable-class-diagram.html) in Chrome or Edge and click **"🖨️ Print / Save as PDF"** (Landscape mode). Each diagram is formatted onto its own clean printable page!

---

## 0. Complete 6-Module System Class Diagram (Whole Group Project Overview)
*(Show this first when the panel asks for the **Complete Class Diagram of the Project**)*

```mermaid
classDiagram
    direction TB

    %% ==================== COMMON MODULE ====================
    class FileHandler {
        <<utility>>
        -Path DATA_DIR$
        +readLines(String)$ List~String~
        +appendLine(String, String)$ void
        +writeLines(String, List~String~)$ void
        +clean(String)$ String
    }
    class IdGenerator {
        <<utility>>
        +nextId(String, List~String~)$ String
    }
    class DataFileException {
        +DataFileException(String, Throwable)
    }
    class GlobalExceptionHandler {
        +handleDataFile(DataFileException, Model) String
        +handleAny(Exception, Model, HttpServletResponse) String
    }

    %% ==================== MODULE 1: USER ====================
    class User {
        <<abstract>>
        -String id
        -String name
        -String email
        -String passwordHash
        -String createdDate
        +getRole()* String
        +getDetails()* String
        +canManageUsers()* boolean
        +getHomePath()* String
        +toFileLine()* String
    }
    class Customer {
        -String phone
        -String address
        +getRole() String
        +getDetails() String
        +canManageUsers() boolean
        +toFileLine() String
    }
    class Admin {
        -String department
        +getRole() String
        +getDetails() String
        +canManageUsers() boolean
        +toFileLine() String
    }
    class UserRepository {
        <<interface>>
        +findAll() List~User~
        +findById(String) Optional~User~
        +findByEmail(String) Optional~User~
        +save(User) User
        +deleteById(String) boolean
    }
    class FileUserRepository {
        -String fileName
        +findAll() List~User~
        +save(User) User
    }
    class UserService {
        -UserRepository repository
        +register(UserForm) User
        +authenticate(String, String) User
        +search(String, String) List~User~
        +deleteUser(String) void
    }
    class UserController {
        -UserService service
        -SessionAuth auth
        +login(...) String
        +register(...) String
        +dashboard(Model) String
        +list(...) String
    }

    %% ==================== MODULE 2: INVENTORY ====================
    class Item {
        <<abstract>>
        -String id
        -String name
        -String category
        -double price
        -int quantity
        -String imageUrl
        +isLowStock() boolean
        +isExpired() boolean
        +isExpiringSoon() boolean
        +toFileLine() String
        +getItemType()* String
        +getSellingPrice()* double
        +getDetails()* String
        #getExtraFields()* String
    }
    class PerishableItem {
        -LocalDate expiryDate
        +isExpired() boolean
        +isExpiringSoon() boolean
        +getSellingPrice() double
        +getDetails() String
    }
    class NonPerishableItem {
        -String brand
        -int shelfLifeMonths
        +getSellingPrice() double
        +getDetails() String
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
        +findAll() List~Item~
        +save(Item) Item
        +deleteById(String) boolean
    }
    class InventoryService {
        -ItemRepository repository
        +search(String, String) List~Item~
        +summary() InventorySummary
        +addItem(ItemForm) Item
        +updateItem(String, ItemForm) Item
        +restockItem(String, int) Item
        +deleteExpiredItems() int
    }
    class InventoryController {
        -InventoryService service
        +shop(...) String
        +list(...) String
        +add(...) String
        +update(...) String
        +restock(...) String
    }

    %% ==================== MODULE 3: SUPPLIER ====================
    class Supplier {
        <<abstract>>
        -String supplierId
        -String companyName
        -String contactNumber
        -String email
        -String supplyCategory
        +getSupplierType()* String
        +calculateLeadTimeDays()* int
        +calculateRestockCost(int, double)* double
        +toFileString()* String
    }
    class LocalFarmerSupplier {
        -String farmLocation
        -boolean organicCertified
        -double freshHandlingFee
        +calculateLeadTimeDays() int
        +calculateRestockCost(int, double) double
    }
    class WholesaleDistributor {
        -String warehouseCode
        -int minOrderQuantity
        -double bulkDiscountRate
        +calculateLeadTimeDays() int
        +calculateRestockCost(int, double) double
    }
    class SupplierRepository {
        <<interface>>
        +findAll() List~Supplier~
        +findById(String) Optional~Supplier~
        +save(Supplier) void
        +deleteById(String) boolean
    }
    class SupplierFileRepository {
        +findAll() List~Supplier~
        +save(Supplier) void
    }
    class SupplierService {
        -SupplierRepository repository
        +getAllSuppliers() List~Supplier~
        +registerFarmer(...) void
        +registerDistributor(...) void
        +calculateProcurement(...) double
    }
    class SupplierController {
        -SupplierService supplierService
        +listSuppliers(...) String
        +addSupplier(...) String
    }

    %% ==================== MODULE 4: ORDER & CART ====================
    class Order {
        -String orderId
        -String userId
        -List~OrderItem~ items
        -double subtotal
        -double discountAmount
        -double netAmount
        -OrderStatus status
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
        +toRecordLine() String
    }
    class CashOrder {
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
    }
    class CardOrder {
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
    }
    class OrderItem {
        -String itemId
        -String itemName
        -int quantity
        -double unitPrice
        +getLineTotal() double
    }
    class CartItem {
        -String cartId
        -String userId
        -String itemId
        -int quantity
        +getLineTotal() double
    }
    class OrderRepository {
        <<interface>>
        +findAll() List~Order~
        +save(Order) Order
        +findCartByUserId(String) List~CartItem~
        +saveCartItem(CartItem) CartItem
    }
    class FileOrderRepository {
        +findAll() List~Order~
        +save(Order) Order
    }
    class OrderService {
        -OrderRepository orderRepository
        +getCartForUser(String) List~CartItem~
        +addToCart(String, String, int) CartItem
        +placeOrder(String, CheckoutForm) Order
        +cancelOrder(String) boolean
    }
    class OrderController {
        -OrderService orderService
        +viewCart(...) String
        +processCheckout(...) String
        +listOrders(...) String
    }

    %% ==================== MODULE 5: PROMOTION ====================
    class Promotion {
        -String promoType
        -String id
        -String name
        -String couponCode
        -double discountPercentage
        -double minimumOrderAmount
        +calculateDiscount(double) double
        +getDiscountDisplay() String
    }
    class PercentagePromotion {
        +calculateDiscount(double) double
        +getDiscountDisplay() String
    }
    class FixedPromotion {
        +calculateDiscount(double) double
        +getDiscountDisplay() String
    }
    class PromotionRepository {
        -String fileName
        +findAll() List~Promotion~
        +save(Promotion) Promotion
        +deleteById(String) void
    }
    class PromotionService {
        -PromotionRepository repository
        +getAllPromotions() List~Promotion~
        +createPromotion(Promotion) Promotion
        +calculateDiscount(String, double) double
    }
    class PromotionController {
        -PromotionService promotionService
        +showPromotions(...) String
        +addPromotion(...) String
    }

    %% ==================== MODULE 6: FEEDBACK ====================
    class Feedback {
        <<abstract>>
        -String id
        -String userId
        -String userName
        -String comment
        -LocalDate createdDate
        +getFeedbackType()* String
        +getReferenceId()* String
        +getSummaryDetails()* String
        +requiresAttention()* boolean
        +toFileLine()* String
    }
    class Review {
        -String itemId
        -int rating
        -String title
        +getSummaryDetails() String
        +requiresAttention() boolean
    }
    class Complaint {
        -String orderId
        -String category
        -String severity
        -String status
        +getSummaryDetails() String
        +requiresAttention() boolean
    }
    class FeedbackRepository {
        <<interface>>
        +findAllReviews() List~Review~
        +saveReview(Review) Review
        +findAllComplaints() List~Complaint~
        +saveComplaint(Complaint) Complaint
    }
    class FileFeedbackRepository {
        +findAllReviews() List~Review~
        +findAllComplaints() List~Complaint~
    }
    class FeedbackService {
        -FeedbackRepository repository
        +searchReviews(...) List~Review~
        +addReview(...) Review
        +searchComplaints(...) List~Complaint~
        +addComplaint(...) Complaint
    }
    class FeedbackController {
        -FeedbackService service
        +listReviews(...) String
        +listComplaints(...) String
    }

    %% ==================== RELATIONSHIPS ====================
    User <|-- Customer
    User <|-- Admin
    UserRepository <|.. FileUserRepository
    UserController *-- UserService
    UserService *-- UserRepository
    FileUserRepository ..> FileHandler
    FileUserRepository ..> IdGenerator

    Item <|-- PerishableItem
    Item <|-- NonPerishableItem
    ItemRepository <|.. FileItemRepository
    InventoryController *-- InventoryService
    InventoryService *-- ItemRepository
    FileItemRepository ..> FileHandler
    FileItemRepository ..> IdGenerator

    Supplier <|-- LocalFarmerSupplier
    Supplier <|-- WholesaleDistributor
    SupplierRepository <|.. SupplierFileRepository
    SupplierController *-- SupplierService
    SupplierService *-- SupplierRepository
    SupplierFileRepository ..> FileHandler
    SupplierService ..> IdGenerator

    Order <|-- CashOrder
    Order <|-- CardOrder
    Order "1" *-- "1..*" OrderItem : Composition
    OrderRepository <|.. FileOrderRepository
    OrderController *-- OrderService
    OrderService *-- OrderRepository
    OrderRepository ..> CartItem
    FileOrderRepository ..> FileHandler
    FileOrderRepository ..> IdGenerator

    Promotion <|-- PercentagePromotion
    Promotion <|-- FixedPromotion
    PromotionController *-- PromotionService
    PromotionService *-- PromotionRepository
    PromotionRepository ..> FileHandler
    PromotionRepository ..> IdGenerator

    Feedback <|-- Review
    Feedback <|-- Complaint
    FeedbackRepository <|.. FileFeedbackRepository
    FeedbackController *-- FeedbackService
    FeedbackService *-- FeedbackRepository
    FileFeedbackRepository ..> FileHandler
    FileFeedbackRepository ..> IdGenerator
```

---

## 1. Member 1 — User & Authentication Module (`com.grocery.user`)
**Storage File:** `data/users.txt` | **ID Prefix:** `U` | **Routes:** `/login`, `/register`, `/users`, `/admin`

```mermaid
classDiagram
    direction TB

    class User {
        <<abstract>>
        -Pattern EMAIL$
        -String id
        -String name
        -String email
        -String passwordHash
        -String createdDate
        #User(String, String, String, String)
        #User(String, String, String, String, String)
        +getId() String
        +setId(String) void
        +getName() String
        +setName(String) void
        +getEmail() String
        +setEmail(String) void
        +getPasswordHash() String
        +setPasswordHash(String) void
        +getCreatedDate() String
        +setCreatedDate(String) void
        #baseLine() String
        +getRole()* String
        +getDetails()* String
        +canManageUsers()* boolean
        +getHomePath()* String
        +toFileLine()* String
    }

    class Customer {
        +String TYPE$
        -Pattern PHONE$
        -String phone
        -String address
        +Customer(String, String, String, String, String, String)
        +Customer(String, String, String, String, String, String, String)
        +getPhone() String
        +setPhone(String) void
        +getAddress() String
        +setAddress(String) void
        +getRole() String
        +getDetails() String
        +canManageUsers() boolean
        +getHomePath() String
        +toFileLine() String
    }

    class Admin {
        +String TYPE$
        -String phone
        -String department
        +Admin(String, String, String, String, String)
        +Admin(String, String, String, String, String, String, String)
        +getPhone() String
        +getDepartment() String
        +setDepartment(String) void
        +getRole() String
        +getDetails() String
        +canManageUsers() boolean
        +getHomePath() String
        +toFileLine() String
    }

    class UserFactory {
        -UserFactory()
        +create(String, String, String, String, String, String, String, String)$ User
        +fromFileLine(String)$ User
    }

    class UserForm {
        -String type
        -String id
        -String name
        -String email
        -String password
        -String confirmPassword
        -String currentPassword
        -String newPassword
        -String phone
        -String address
        -String department
        +from(User)$ UserForm
    }

    class PasswordUtil {
        <<utility>>
        -int ITERATIONS$
        -int KEY_LENGTH$
        -int SALT_LENGTH$
        -PasswordUtil()
        +hash(String)$ String
        +verify(String, String)$ boolean
    }

    class UserRepository {
        <<interface>>
        +findAll() List~User~
        +findById(String) Optional~User~
        +findByEmail(String) Optional~User~
        +save(User) User
        +deleteById(String) boolean
    }

    class FileUserRepository {
        -String fileName
        +FileUserRepository(String)
        +findAll() List~User~
        +findById(String) Optional~User~
        +findByEmail(String) Optional~User~
        +save(User) User
        +deleteById(String) boolean
        -writeAll(List~User~) void
        -nextId(List~User~) String
    }

    class UserService {
        -UserRepository repository
        +UserService(UserRepository)
        +seedDefaults() void
        +register(UserForm) User
        +createUser(UserForm) User
        +findById(String) Optional~User~
        +getById(String) User
        +search(String, String) List~User~
        +summary() UserSummary
        +authenticate(String, String) User
        +updateProfile(String, UserForm) User
        +adminUpdate(String, UserForm) User
        +changePassword(String, UserForm) void
        +deleteOwnAccount(String, String) void
        +deleteUser(String) void
    }

    class SessionAuth {
        -UserService userService
        +SessionAuth(UserService)
        +currentUser(HttpSession) User
        +login(HttpServletRequest, User) void
        +refresh(HttpSession, User) void
        +logout(HttpSession) void
        +preHandle(HttpServletRequest, HttpServletResponse, Object) boolean
    }

    class UserController {
        -UserService service
        -SessionAuth auth
        +UserController(UserService, SessionAuth)
        +showRegister(HttpSession, Model) String
        +register(UserForm, Model, RedirectAttributes) String
        +showLogin(String, HttpSession, Model) String
        +login(UserForm, String, HttpServletRequest, Model) String
        +logout(HttpSession) String
        +showProfile(HttpSession, Model, RedirectAttributes) String
        +updateProfile(UserForm, HttpSession, Model, RedirectAttributes) String
        +dashboard(Model) String
        +list(String, String, HttpSession, Model, RedirectAttributes) String
        +add(UserForm, HttpSession, Model, RedirectAttributes) String
        +update(String, UserForm, HttpSession, Model, RedirectAttributes) String
        +delete(String, HttpSession, RedirectAttributes) String
    }

    User <|-- Customer : Inheritance (is-a)
    User <|-- Admin : Inheritance (is-a)
    UserRepository <|.. FileUserRepository : Realization (implements)
    UserController *-- UserService : Composition (has-a)
    UserController *-- SessionAuth : Composition (has-a)
    SessionAuth *-- UserService : Association (has-a)
    UserService *-- UserRepository : Composition (has-a)
    UserService ..> UserFactory : Dependency (uses)
    UserService ..> PasswordUtil : Dependency (uses)
    UserService ..> UserForm : Dependency (uses)
    FileUserRepository ..> UserFactory : Dependency (uses)
```

---

## 2. Member 2 (YOU) — Grocery Inventory & Storefront + Common Module (`com.grocery.inventory` & `com.grocery.common`)
**Storage File:** `data/items.txt` | **ID Prefix:** `ITM` | **Routes:** `/inventory`, `/shop`

```mermaid
classDiagram
    direction TB

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
        +setId(String) void
        +getName() String
        +setName(String) void
        +getCategory() String
        +setCategory(String) void
        +getPrice() double
        +setPrice(double) void
        +getQuantity() int
        +setQuantity(int) void
        +getImageUrl() String
        +setImageUrl(String) void
        +isLowStock() boolean
        +isExpired() boolean
        +isExpiringSoon() boolean
        +toFileLine() String
        #clean(String)$ String
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

    class ItemForm {
        -String type
        -String id
        -String name
        -String category
        -Double price
        -Integer quantity
        -String imageUrl
        -LocalDate expiryDate
        -String brand
        -Integer shelfLifeMonths
        +from(Item)$ ItemForm
    }

    class InventorySummary {
        <<record>>
        +int total
        +int lowStock
        +int expiringSoon
        +int expired
        +double stockValue
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
        +shopDeals() List~ProductView~
        +findShopProduct(String) Optional~ProductView~
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
    InventoryController *-- InventoryService : Composition (has-a)
    InventoryService *-- ItemRepository : Composition (has-a)
    InventoryService ..> ItemFactory : Dependency (uses)
    InventoryService ..> ItemForm : Dependency (uses)
    InventoryService ..> InventorySummary : Dependency (creates)
    FileItemRepository ..> ItemFactory : Dependency (uses)
    FileItemRepository ..> FileHandler : Dependency (uses)
    FileItemRepository ..> IdGenerator : Dependency (uses)
```

---

## 3. Member 3 — Supplier & Vendor Logistics Module (`com.grocery.supplier`)
**Storage File:** `data/suppliers.txt` | **ID Prefix:** `SUP` | **Routes:** `/suppliers`

```mermaid
classDiagram
    direction TB

    class Supplier {
        <<abstract>>
        -String supplierId
        -String companyName
        -String contactNumber
        -String email
        -String supplyCategory
        +Supplier()
        +Supplier(String, String, String, String, String)
        +getSupplierId() String
        +setSupplierId(String) void
        +getCompanyName() String
        +setCompanyName(String) void
        +getContactNumber() String
        +setContactNumber(String) void
        +getEmail() String
        +setEmail(String) void
        +getSupplyCategory() String
        +setSupplyCategory(String) void
        +getSupplierType()* String
        +calculateLeadTimeDays()* int
        +calculateRestockCost(int, double)* double
        +toFileString()* String
        +getSupplierSummary() String
    }

    class LocalFarmerSupplier {
        -String farmLocation
        -boolean organicCertified
        -double freshHandlingFee
        +LocalFarmerSupplier()
        +LocalFarmerSupplier(String, String, String, String, String, String, boolean, double)
        +getFarmLocation() String
        +setFarmLocation(String) void
        +isOrganicCertified() boolean
        +setOrganicCertified(boolean) void
        +getFreshHandlingFee() double
        +setFreshHandlingFee(double) void
        +getSupplierType() String
        +calculateLeadTimeDays() int
        +calculateRestockCost(int, double) double
        +toFileString() String
    }

    class WholesaleDistributor {
        -String warehouseCode
        -int minOrderQuantity
        -double bulkDiscountRate
        +WholesaleDistributor()
        +WholesaleDistributor(String, String, String, String, String, String, int, double)
        +getWarehouseCode() String
        +setWarehouseCode(String) void
        +getMinOrderQuantity() int
        +setMinOrderQuantity(int) void
        +getBulkDiscountRate() double
        +setBulkDiscountRate(double) void
        +getSupplierType() String
        +calculateLeadTimeDays() int
        +calculateRestockCost(int, double) double
        +toFileString() String
    }

    class SupplierRepository {
        <<interface>>
        +findAll() List~Supplier~
        +findById(String) Optional~Supplier~
        +save(Supplier) void
        +update(Supplier) void
        +deleteById(String) boolean
        +search(String) List~Supplier~
        +search(String, String) List~Supplier~
    }

    class SupplierFileRepository {
        -String FILE_NAME$
        +findAll() List~Supplier~
        +findById(String) Optional~Supplier~
        +save(Supplier) void
        +update(Supplier) void
        +deleteById(String) boolean
        +search(String) List~Supplier~
        +search(String, String) List~Supplier~
        -parseLine(String) Supplier
    }

    class SupplierService {
        -SupplierRepository repository
        +SupplierService(SupplierRepository)
        +getAllSuppliers() List~Supplier~
        +getSupplierById(String) Optional~Supplier~
        +searchSuppliers(String) List~Supplier~
        +filterSuppliers(String, String) List~Supplier~
        +generateNextId() String
        +registerFarmer(...) void
        +registerDistributor(...) void
        +updateSupplier(...) boolean
        +deleteSupplier(String) boolean
        +calculateProcurement(String, int, double) double
    }

    class SupplierController {
        -SupplierService supplierService
        +SupplierController(SupplierService)
        +listSuppliers(String, String, String, Model) String
        +addSupplier(...) String
        +updateSupplier(...) String
        +deleteSupplier(String, RedirectAttributes) String
    }

    Supplier <|-- LocalFarmerSupplier : Inheritance (is-a)
    Supplier <|-- WholesaleDistributor : Inheritance (is-a)
    SupplierRepository <|.. SupplierFileRepository : Realization (implements)
    SupplierController *-- SupplierService : Composition (has-a)
    SupplierService *-- SupplierRepository : Composition (has-a)
```

---

## 4. Member 4 — Cart & Order Processing Module (`com.grocery.order`)
**Storage Files:** `data/orders.txt` & `data/carts.txt` | **ID Prefixes:** `ORD`, `CRT` | **Routes:** `/cart`, `/orders`

```mermaid
classDiagram
    direction TB

    class Order {
        -String orderId
        -String userId
        -List~OrderItem~ items
        -double subtotal
        -double discountAmount
        -double netAmount
        -OrderStatus status
        -String orderDate
        -String shippingAddress
        -String contactPhone
        -String paymentMethod
        -String promoCode
        +Order()
        +Order(String, String, List~OrderItem~, double, double, double, OrderStatus, String, String, String, String, String)
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
        +createTypedOrder(...)$ Order
        +toRecordLine() String
        +fromRecordLine(String)$ Order
        +getTotalQuantity() int
        +canBeCancelled() boolean
        +builder()$ Builder
    }

    class CashOrder {
        +CashOrder(String, String, List~OrderItem~, double, double, double, OrderStatus, String, String, String, String)
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
    }

    class CardOrder {
        +CardOrder(String, String, List~OrderItem~, double, double, double, OrderStatus, String, String, String, String)
        +requiresCardTerminal() boolean
        +getPaymentInstruction() String
    }

    class OrderItem {
        -String itemId
        -String itemName
        -int quantity
        -double unitPrice
        +OrderItem(String, String, int, double)
        +getLineTotal() double
        +toToken() String
        +fromToken(String)$ OrderItem
    }

    class CartItem {
        -String cartId
        -String userId
        -String itemId
        -int quantity
        -String addedDate
        -String itemName
        -double unitPrice
        -String unit
        +CartItem(String, String, String, int, String)
        +getLineTotal() double
        +toRecordLine() String
        +fromRecordLine(String)$ CartItem
    }

    class OrderStatus {
        <<enumeration>>
        PENDING
        CONFIRMED
        SHIPPED
        DELIVERED
        CANCELLED
        +getDisplayName() String
        +getBadgeClass() String
        +fromString(String)$ OrderStatus
    }

    class CheckoutForm {
        -String fullName
        -String contactPhone
        -String shippingAddress
        -String deliveryNotes
        -String paymentMethod
        -String promoCode
    }

    class OrderRepository {
        <<interface>>
        +findAll() List~Order~
        +findById(String) Optional~Order~
        +findByUserId(String) List~Order~
        +save(Order) Order
        +updateStatus(String, OrderStatus) boolean
        +deleteById(String) void
        +findAllCartItems() List~CartItem~
        +findCartByUserId(String) List~CartItem~
        +findCartById(String) Optional~CartItem~
        +findCartByUserAndItem(String, String) Optional~CartItem~
        +saveCartItem(CartItem) CartItem
        +deleteCartById(String) void
        +deleteCartByUserId(String) void
    }

    class FileOrderRepository {
        -String ordersFile
        -String cartsFile
        +FileOrderRepository(String, String)
        +findAll() List~Order~
        +findById(String) Optional~Order~
        +save(Order) Order
        +updateStatus(String, OrderStatus) boolean
        +deleteById(String) void
        +findCartByUserId(String) List~CartItem~
        +saveCartItem(CartItem) CartItem
        +deleteCartById(String) void
        +deleteCartByUserId(String) void
    }

    class OrderService {
        -OrderRepository orderRepository
        +OrderService(OrderRepository)
        +getCartForUser(String) List~CartItem~
        +addToCart(String, String, int) CartItem
        +updateQuantity(String, int) void
        +removeFromCart(String) void
        +clearCart(String) void
        +calculateSubtotal(List~CartItem~) double
        +placeOrder(String, CheckoutForm) Order
        +getOrderById(String) Optional~Order~
        +getOrdersForUser(String) List~Order~
        +getAllOrders() List~Order~
        +cancelOrder(String) boolean
        +updateOrderStatus(String, OrderStatus) boolean
        +deleteOrder(String) void
        +calculatePromoDiscount(String, double) double
    }

    class OrderController {
        -OrderService orderService
        +OrderController(OrderService)
        +viewCart(Model, HttpSession) String
        +addToCart(String, int, HttpSession, RedirectAttributes) String
        +updateQuantity(String, int, RedirectAttributes) String
        +removeItem(String, RedirectAttributes) String
        +clearCart(HttpSession, RedirectAttributes) String
        +listOrders(Model, HttpSession) String
        +checkoutPage(Model, HttpSession, RedirectAttributes) String
        +processCheckout(CheckoutForm, BindingResult, Model, HttpSession, RedirectAttributes) String
        +orderSuccess(String, Model, RedirectAttributes) String
        +orderDetails(String, Model, RedirectAttributes) String
        +cancelOrder(String, RedirectAttributes) String
        +updateStatus(String, String, RedirectAttributes) String
        +deleteOrder(String, RedirectAttributes) String
    }

    Order <|-- CashOrder : Inheritance (is-a)
    Order <|-- CardOrder : Inheritance (is-a)
    Order "1" *-- "1..*" OrderItem : Composition (has-a)
    Order --> OrderStatus : Association
    OrderRepository <|.. FileOrderRepository : Realization (implements)
    OrderController *-- OrderService : Composition (has-a)
    OrderService *-- OrderRepository : Composition (has-a)
    OrderService ..> CartItem : Dependency (uses)
    OrderService ..> CheckoutForm : Dependency (uses)
```

---

## 5. Member 5 — Discounts & Promo Codes Module (`com.grocery.promo`)
**Storage File:** `data/promotions.txt` | **ID Prefix:** `PRM` | **Routes:** `/promos`

```mermaid
classDiagram
    direction TB

    class Promotion {
        +String TYPE_PERCENTAGE$
        +String TYPE_FIXED$
        -String promoType
        -String id
        -String name
        -String description
        -String couponCode
        -double discountPercentage
        -double minimumOrderAmount
        -LocalDate startDate
        -LocalDate expiryDate
        -boolean active
        +Promotion()
        +Promotion(String, String, String, String, String, double, double, LocalDate, LocalDate, boolean)
        +calculateDiscount(double) double
        +getDiscountDisplay() String
        +createTyped(Promotion)$ Promotion
        +toFileLine() String
        +fromFileLine(String)$ Promotion
        +getPromoType() String
        +setPromoType(String) void
        +getId() String
        +setId(String) void
        +getName() String
        +setName(String) void
        +getCouponCode() String
        +setCouponCode(String) void
        +getDiscountPercentage() double
        +setDiscountPercentage(double) void
        +getMinimumOrderAmount() double
        +setMinimumOrderAmount(double) void
        +isActive() boolean
        +setActive(boolean) void
    }

    class PercentagePromotion {
        +PercentagePromotion(String, String, String, String, double, double, LocalDate, LocalDate, boolean)
        +calculateDiscount(double) double
        +getDiscountDisplay() String
    }

    class FixedPromotion {
        +FixedPromotion(String, String, String, String, double, double, LocalDate, LocalDate, boolean)
        +calculateDiscount(double) double
        +getDiscountDisplay() String
    }

    class PromotionRepository {
        -String fileName
        +PromotionRepository(String)
        +seedDefaults() void
        +findAll() List~Promotion~
        +findById(String) Optional~Promotion~
        +findByCouponCodeIgnoreCase(String) Optional~Promotion~
        +findByActiveTrue() List~Promotion~
        +save(Promotion) Promotion
        +existsById(String) boolean
        +deleteById(String) void
        -writeAll(List~Promotion~) void
    }

    class PromotionService {
        -PromotionRepository repository
        +PromotionService(PromotionRepository)
        +getAllPromotions() List~Promotion~
        +searchPromotions(String) List~Promotion~
        +getActivePromotions() List~Promotion~
        +getPromotionById(String) Optional~Promotion~
        +createPromotion(Promotion) Promotion
        +updatePromotion(String, Promotion) Promotion
        +deletePromotion(String) void
        +findPromotionByCouponCode(String) Optional~Promotion~
        +isCouponValid(String, double) boolean
        +calculateDiscount(String, double) double
        -validatePromotion(Promotion) void
    }

    class PromotionController {
        -PromotionService promotionService
        +PromotionController(PromotionService)
        +showPromotions(String, String, Model) String
        +addPromotion(Promotion, RedirectAttributes) String
        +updatePromotion(String, Promotion, RedirectAttributes) String
        +deletePromotion(String, RedirectAttributes) String
    }

    Promotion <|-- PercentagePromotion : Inheritance (is-a)
    Promotion <|-- FixedPromotion : Inheritance (is-a)
    PromotionController *-- PromotionService : Composition (has-a)
    PromotionService *-- PromotionRepository : Composition (has-a)
    PromotionRepository ..> Promotion : Dependency (manages)
```

---

## 6. Member 6 — Product Reviews & Customer Complaints Module (`com.grocery.feedback`)
**Storage File:** `data/feedbacks.txt` | **ID Prefixes:** `REV`, `CMP` | **Routes:** `/reviews`, `/complaints`

```mermaid
classDiagram
    direction TB

    class Feedback {
        <<abstract>>
        -String id
        -String userId
        -String userName
        -String comment
        -LocalDate createdDate
        #Feedback(String, String, String, String, LocalDate)
        +getId() String
        +setId(String) void
        +getUserId() String
        +setUserId(String) void
        +getUserName() String
        +setUserName(String) void
        +getComment() String
        +setComment(String) void
        +getCreatedDate() LocalDate
        +setCreatedDate(LocalDate) void
        #clean(String)$ String
        +getFeedbackType()* String
        +getTypeLabel()* String
        +getReferenceId()* String
        +getSummaryDetails()* String
        +getBadgeText()* String
        +requiresAttention()* boolean
        +toFileLine()* String
    }

    class Review {
        +String TYPE$
        +String PREFIX$
        -int LOW_RATING_LIMIT$
        -String itemId
        -int rating
        -String title
        +Review(String, String, String, String, int, String, String, LocalDate)
        +getItemId() String
        +setItemId(String) void
        +getRating() int
        +setRating(int) void
        +getTitle() String
        +setTitle(String) void
        +getStars() String
        +isPositive() boolean
        +getFeedbackType() String
        +getTypeLabel() String
        +getReferenceId() String
        +getSummaryDetails() String
        +getBadgeText() String
        +requiresAttention() boolean
        +toFileLine() String
    }

    class Complaint {
        +String TYPE$
        +String PREFIX$
        +String STATUS_OPEN$
        +String STATUS_IN_PROGRESS$
        +String STATUS_RESOLVED$
        -String orderId
        -String category
        -String severity
        -String status
        -String resolutionNote
        +Complaint(String, String, String, String, String, String, String, String, String, LocalDate)
        +getOrderId() String
        +setOrderId(String) void
        +getCategory() String
        +setCategory(String) void
        +getSeverity() String
        +setSeverity(String) void
        +getStatus() String
        +setStatus(String) void
        +getResolutionNote() String
        +setResolutionNote(String) void
        +isResolved() boolean
        +getFeedbackType() String
        +getTypeLabel() String
        +getReferenceId() String
        +getSummaryDetails() String
        +getBadgeText() String
        +requiresAttention() boolean
        +toFileLine() String
    }

    class FeedbackFactory {
        -FeedbackFactory()
        +createReview(String, String, String, String, Integer, String, String, LocalDate)$ Review
        +createComplaint(String, String, String, String, String, String, String, String, String, LocalDate)$ Complaint
        +reviewFromFileLine(String)$ Review
        +complaintFromFileLine(String)$ Complaint
    }

    class FeedbackForm {
        -String type
        -String id
        -String comment
        -String itemId
        -Integer rating
        -String title
        -String orderId
        -String category
        -String severity
        -String status
        -String resolutionNote
        +from(Feedback)$ FeedbackForm
    }

    class FeedbackRepository {
        <<interface>>
        +findAllReviews() List~Review~
        +findReviewById(String) Optional~Review~
        +saveReview(Review) Review
        +deleteReviewById(String) boolean
        +findAllComplaints() List~Complaint~
        +findComplaintById(String) Optional~Complaint~
        +saveComplaint(Complaint) Complaint
        +deleteComplaintById(String) boolean
    }

    class FileFeedbackRepository {
        -String feedbacksFile
        +FileFeedbackRepository(String)
        +findAllReviews() List~Review~
        +findReviewById(String) Optional~Review~
        +saveReview(Review) Review
        +deleteReviewById(String) boolean
        +findAllComplaints() List~Complaint~
        +findComplaintById(String) Optional~Complaint~
        +saveComplaint(Complaint) Complaint
        +deleteComplaintById(String) boolean
        -writeAll(List~Review~, List~Complaint~) void
    }

    class FeedbackService {
        -FeedbackRepository repository
        +FeedbackService(FeedbackRepository)
        +seedDefaults() void
        +searchReviews(String, Integer) List~Review~
        +getReviewsForItem(String) List~Review~
        +getReviewById(String) Review
        +addReview(FeedbackForm, String, String) Review
        +updateReview(String, FeedbackForm, String, boolean) Review
        +deleteReview(String, String, boolean) void
        +searchComplaints(String, String, String, boolean) List~Complaint~
        +getComplaintById(String) Complaint
        +addComplaint(FeedbackForm, String, String) Complaint
        +updateComplaint(String, FeedbackForm, String, boolean) Complaint
        +deleteComplaint(String, String, boolean) void
        +summary(String, boolean) FeedbackSummary
    }

    class FeedbackController {
        -FeedbackService service
        +FeedbackController(FeedbackService)
        +listReviews(String, Integer, HttpSession, Model) String
        +showAddReviewForm(String, HttpSession, Model) String
        +addReview(FeedbackForm, HttpSession, Model, RedirectAttributes) String
        +showEditReviewForm(String, HttpSession, Model, RedirectAttributes) String
        +updateReview(String, FeedbackForm, HttpSession, Model, RedirectAttributes) String
        +deleteReview(String, HttpSession, RedirectAttributes) String
        +listComplaints(String, String, HttpSession, Model) String
        +showAddComplaintForm(String, HttpSession, Model) String
        +addComplaint(FeedbackForm, HttpSession, Model, RedirectAttributes) String
        +showEditComplaintForm(String, HttpSession, Model, RedirectAttributes) String
        +updateComplaint(String, FeedbackForm, HttpSession, Model, RedirectAttributes) String
        +deleteComplaint(String, HttpSession, RedirectAttributes) String
    }

    Feedback <|-- Review : Inheritance (is-a)
    Feedback <|-- Complaint : Inheritance (is-a)
    FeedbackRepository <|.. FileFeedbackRepository : Realization (implements)
    FeedbackController *-- FeedbackService : Composition (has-a)
    FeedbackService *-- FeedbackRepository : Composition (has-a)
    FeedbackService ..> FeedbackFactory : Dependency (uses)
    FeedbackService ..> FeedbackForm : Dependency (uses)
    FileFeedbackRepository ..> FeedbackFactory : Dependency (uses)
```
