# Online Grocery Order Management System

SE1020 group project. A web application built with **Java, Spring Boot, Thymeleaf, HTML, CSS and JavaScript**.
Data is stored in plain text files (no database).

## 1. Team and modules

| # | Module | Java package | Templates folder | CSS / JS | Data file(s) | ID prefix | URL prefix | Owner (GitHub) |
|---|--------|--------------|------------------|----------|--------------|-----------|------------|----------------|
| 1 | User & Authentication | `user` | `templates/user` | `user.css` / `user.js` | `users.txt` | `U` | `/users` | @MrHasit |
| 2 | Grocery Inventory & Items | `inventory` | `templates/inventory` | `inventory.css` / `inventory.js` | `items.txt` | `ITM` | `/items` | @bimsaramaleesha |
| 3 | Supplier & Vendor Logistics | `supplier` | `templates/supplier` | `supplier.css` / `supplier.js` | `suppliers.txt`, `deliveries.txt` | `SUP` | `/suppliers` | @Thilanjana01 |
| 4 | Cart & Order Processing | `order` | `templates/order` | `order.css` / `order.js` | `carts.txt`, `orders.txt` | `CRT`, `ORD` | `/cart`, `/orders` | @ |
| 5 | Discounts & Promo Codes | `promo` | `templates/promo` | `promo.css` / `promo.js` | `promos.txt` | `PRM` | `/promos` | @ |
| 6 | Reviews & Customer Complaints | `feedback` | `templates/feedback` | `feedback.css` / `feedback.js` | `reviews.txt`, `complaints.txt` | `REV`, `CMP` | `/reviews`, `/complaints` | @ |

**Review buddies** (you review each other's pull requests): 1 and 2, 3 and 4, 5 and 6.

## 2. First-time setup

1. Install **JDK 17 or newer** and **IntelliJ IDEA**.
2. Accept the repository invitation sent to your email (GitHub).
3. Tell Git who you are. Use the **same email as your GitHub account**, otherwise your commits will not count for you:
   ```
   git config --global user.name "Your Name"
   git config --global user.email "your-github-email@example.com"
   ```
4. In IntelliJ: **File > New > Project from Version Control**, paste the repository URL, choose a folder **outside OneDrive** (for example `C:\Projects`), and click Clone.
5. Wait for Maven to finish loading (progress bar at the bottom). If asked, set the project JDK to 17 or newer.
6. Run `GroceryApplication` (green play button) and open http://localhost:8080. You should see the home page.
7. Switch to the `develop` branch before you start any work (see section 3).

## 3. Branches and daily workflow

```
main      stable, demo-ready code. Only the leader merges here.
develop   everyone's work comes together here.
feature/<module>-<task>   your own working branch, e.g. feature/inventory-add-item
```

**Nobody pushes directly to `main` or `develop`. Everything goes through a pull request (PR).**

Every task:

```
git checkout develop
git pull
git checkout -b feature/inventory-add-item     # new branch for this task

# ... write code, then commit small and often ...
git add src/main/java/com/grocery/inventory src/main/resources/templates/inventory
git commit -m "feat(inventory): add item form"

git pull origin develop      # bring in teammates' latest work, run the app again
git push -u origin feature/inventory-add-item
```

Then on GitHub click **Compare & pull request**, base branch **develop**, fill in the template, and ask your review buddy to review.
When it is approved, click **Create a merge commit** (never "Squash and merge", it hides your individual commits).
After merging, go back to `develop`, `git pull`, and start the next task from a new branch.

### Commit messages
```
feat(inventory): add create item form
fix(user): handle duplicate username
docs(report): add class diagram
```
Prefixes: `feat`, `fix`, `refactor`, `style`, `docs`, `test`. Commit **often** (several times a week). Commit history is part of your marks.

### Reviewing a pull request
Open the PR, check "Files changed", leave a comment on anything unclear, then **Review changes > Approve**.
Pull the branch and run it if you can. Nobody approves their own PR.

## 4. Rules

1. **Stay in your lane.** Only edit your own Java package, templates folder, CSS file and JS file.
2. **Shared files belong to the leader:** everything in `common/`, `templates/fragments/`, `common.css`, `common.js`, `pom.xml`, `application.properties`, `.github/`. Need a change? Message the leader or open an Issue.
3. **Use `FileHandler` for every file read/write.** Do not write your own file code.
4. **One line = one record, fields separated by `|`**, for example `ITM001|Milk|250.00|40`. Use `FileHandler.clean(text)` on text typed by users, and `split("\\|")` to read a line.
5. **IDs:** use `IdGenerator.nextId("ITM", existingIds)`. Prefixes are in the table above.
6. **Talk to other modules by ID only.** If you need an item's price, store the item ID and ask the inventory module's service. Do not copy or edit another module's classes.
7. **Start new pages from** `templates/fragments/page-template.html`. Prefix your CSS classes with your module (`.inv-card`, `.cart-table`).
8. **Never reformat or "optimize imports" on files you do not own.**
9. **Never commit the `data/` folder.** It is in `.gitignore`, and your test data stays on your own computer.
10. **Do not push anything that does not start.** Run the app before every pull request.
11. **Every member must understand all the code they submit.** The viva asks about it.

## 5. Project structure

```
src/main/java/com/grocery/
    common/        shared helpers (leader only)
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

## 6. Something went wrong?

| Problem | Fix |
|---------|-----|
| `git push` rejected | Run `git pull origin develop`, solve any conflict, run the app, push again |
| Merge conflict | Open the file, keep the correct lines, delete the `<<<<<<<`, `=======`, `>>>>>>>` markers, then `git add` and `git commit`. Ask the leader if unsure |
| Committed to the wrong branch | Do not push. Message the leader |
| App will not start | Read the **first** red error in the console and send it to the group |
| Port 8080 already in use | Stop the other running app (red square in IntelliJ) |
