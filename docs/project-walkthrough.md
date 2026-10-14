# Project Walkthrough (read this first)

How the whole system works, what every folder and file is for, and how each member plugs their module in.

## 1. The big picture

The browser asks for a page, Spring Boot finds the right method, the method gets data, and Thymeleaf turns an HTML template into the finished page.

```
Browser  ->  Controller  ->  Service  ->  Repository  ->  FileHandler  ->  data/xxx.txt
   ^            |
   |            v
   +-------  Thymeleaf template (HTML + data from the controller's Model)
```

| Layer | What it does | Rule of thumb |
|-------|--------------|---------------|
| **Controller** | One method per URL. Reads the request, calls the service, picks a page or a redirect. | No business rules here. |
| **Service** | The business rules (validation, search, calculations). | The controller only talks to this. |
| **Repository** | Reads and writes the `.txt` file. Interface (`XxxRepository`) + implementation (`FileXxxRepository`). | Only place that touches files. |
| **Model** | The OOP classes (parent class, children, validation in setters). | This is where the viva marks are. |
| **DTO / Form** | A plain carrier for HTML form values. | Needed when the model is abstract. |
| **Template** | Thymeleaf HTML page wrapped in the shared layout. | Uses Bootstrap + your own CSS. |
| **FileHandler** | Shared helper for every file read/write (leader owns it). | Never write your own file code. |

GET means "show me a page"; POST means "I'm sending data". After a POST we always **redirect** so a browser refresh does not submit twice.

## 2. Folder map

```
online-grocery-system/
  pom.xml                      libraries and Java version (leader only)
  mvnw / mvnw.cmd              Maven wrapper: builds the app without installing Maven
  README.md                    project overview + how to run
  CONTRIBUTING.md              team rules and the Git workflow (everyone reads it)
  data/                        LIVE data files (items.txt ...). NOT in Git. Created automatically.
  sample-data/                 demo data you CAN commit; copy a file into data/ to demo
  docs/
    module-pattern.md          the pattern to copy for a new module
    data-formats.md            the exact line format of every .txt file + links between modules
    project-walkthrough.md     this file
    class-diagrams/, report/   for the final submission
  .github/
    CODEOWNERS                 GitHub asks the module owner to review changes to their folders
    pull_request_template.md   the checklist shown on every pull request
    workflows/build.yml        build check: every pull request is compiled and tested automatically
  src/main/java/com/grocery/
    GroceryApplication.java    the start button (main method)
    common/                    SHARED CODE (leader only)
      FileHandler.java           readLines / appendLine / writeLines / clean, always inside data/
      IdGenerator.java           makes IDs: nextId("ITM", existingIds) -> ITM001, ITM002 ...
      DataFileException.java     thrown when a file cannot be read or written
      GlobalExceptionHandler.java  turns any error into the friendly error.html page
      HomeController.java        serves the home page "/"
    inventory/ user/ supplier/ order/ promo/ feedback/    one package per module, each with
      controller/ service/ repository/ model/ (inventory also has dto/)
  src/main/resources/
    application.properties     settings (port 8080, no template cache, our own error page)
    templates/
      fragments/layout.html      shared navbar, footer, Bootstrap, head fragments (leader only)
      fragments/page-template.html  copy this to start a new page
      index.html, error.html     home and error pages
      <module>/                  your pages
    static/css/common.css, js/common.js   shared styles / scripts (leader only)
    static/css/<module>.css, js/<module>.js   your own styles / scripts
  src/test/java/               automatic tests (build.yml runs them on every pull request)
```

## 3. The shared layout (how pages get a navbar)

Every page is a small HTML file that pulls in shared pieces:

```html
<head th:replace="~{fragments/layout :: headWithCss('Inventory', '/css/inventory.css')}"></head>
<body>
<nav th:replace="~{fragments/layout :: navbar}"></nav>
<main class="container my-4"> ...your content... </main>
<footer th:replace="~{fragments/layout :: footer}"></footer>
<th:block th:replace="~{fragments/layout :: scripts}"></th:block>
<script th:src="@{/js/inventory.js}"></script>
</body>
```

- `head('Title')` loads Bootstrap + `common.css`. `headWithCss('Title', '/css/x.css')` also loads your stylesheet. A `<link>` written inside a `th:replace` head would be thrown away, so always use `headWithCss` for your own CSS.
- Start every CSS class with your module prefix (`inv-`, `sup-`, `ord-`, `usr-`, `prm-`, `fb-`) so styles never clash.
- Thymeleaf cheat sheet: `th:text="${x}"` shows a value, `th:each="i : ${list}"` repeats, `th:if` shows conditionally, `th:href="@{/inventory/new}"` builds a URL, `th:field="*{name}"` connects a form input to the form object.

## 4. Data files

- One record per line, fields separated by `|`. First field is the **type** when the module has a parent class with children (PERISHABLE, LOCAL ...).
- `FileHandler` always works inside `data/`, so you pass only the file name: `FileHandler.readLines("suppliers.txt")`.
- Run user-typed text through `FileHandler.clean(text)` before writing it (it removes `|` and line breaks).
- Every model has `toFileLine()` (object -> line) and a factory or constructor that reads a line back. Polymorphism: each child writes its own extra fields.
- Exact formats and links between modules: see [data-formats.md](data-formats.md).

## 5. The reference module: Inventory (read it in this order)

1. `model/Item.java`: abstract parent, private fields, validating setters, abstract methods (**abstraction, encapsulation**)
2. `PerishableItem` / `NonPerishableItem`: children with their own fields and `getSellingPrice()` / `getDetails()` (**inheritance, polymorphism**)
3. `ItemFactory`: builds the right child from form values or a file line
4. `dto/ItemForm`: carries the HTML form values; `dto/InventorySummary`: the numbers on the cards
5. `repository/ItemRepository` (WHAT) and `FileItemRepository` (HOW, via `FileHandler`)
6. `service/InventoryService`: search, filters, add / update / delete, summary
7. `controller/InventoryController`: `GET /inventory`, `/inventory/new`, `/inventory/{id}/edit`, `POST` for save and delete
8. `templates/inventory/list.html`, `form.html`, `static/css/inventory.css`, `static/js/inventory.js`

## 6. How each member starts (checklist)

1. Clone, switch to `develop`, run the app, open <http://localhost:8080>.
2. Create `feature/<module>-<task>` from `develop`.
3. Copy the inventory pattern: `XxxRepository` + `FileXxxRepository`, `XxxService`, `XxxController`, model classes with a parent class and at least two children, `list` and `form` pages.
4. Use `FileHandler` for files, `IdGenerator` for IDs, the shared layout for pages, your own prefix for CSS.
5. Run the app and click through every page. Run `./mvnw verify`.
6. Commit small and often (`feat(module): what I did`), push, open a pull request into `develop`, wait for the green build check, ask your review buddy.

Minimum for marks per member: 3 CRUD operations, 3 pages, inheritance + polymorphism, file handling, and you must be able to explain every line.

## 7. What each module must offer the others

| Module | Others need from it |
|--------|---------------------|
| User | On login, put the user's ID in the session: `session.setAttribute("userId", "U001")`. Cart and orders read it (they use `U001` if nobody is logged in). |
| Inventory | `InventoryService.search(null)` and `getById(id)` return items with `getSellingPrice()`, `getQuantity()`, `isExpired()`. |
| Supplier | Restock info by item ID (optional link to inventory). |
| Order | Reads items through `InventoryService` (not by parsing `items.txt`), reads promos from `promos.txt`. |
| Promo | Must write `promos.txt` in the format in `data-formats.md`; Order applies it at checkout. |
| Feedback | Link reviews to an item ID and complaints to an order ID. Do not copy their classes. |

Golden rule: **talk to other modules by ID, through their service, never by editing their files or parsing their data file.**
