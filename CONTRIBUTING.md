# Contributing Guide

How our 6-person team works together on this repo. Follow these steps every time, and nobody's work gets overwritten.

## The golden rules

1. **Never commit directly to `main` or `develop`.** They are protected. All work goes through a Pull Request (PR).
2. **One module per person.** Work inside your own module's package, template folder, CSS and JS file.
3. **Pull before you start.** Always begin from the latest `develop`.
4. **Small and often.** Small commits and small PRs are easy to review. A giant PR after two weeks is where conflicts happen.
5. **Ask before touching shared files** (`common/`, `layout.html`, `pom.xml`). Tell the team leader first.

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Stable versions only. The leader updates it at milestones. |
| `develop` | Integration branch. All PRs go here. |
| `feature/<module>-<short-description>` | Your working branches. |

**Branch name examples:**

```
feature/auth-login-page
feature/inventory-add-item
feature/orders-cart-total
fix/promo-expiry-date
```

Use lowercase and hyphens, no spaces. Delete your branch after it is merged.

## Daily workflow (step by step)

### Step 1: Get the latest code

```bash
git checkout develop
git pull origin develop
```

### Step 2: Create your feature branch

```bash
git checkout -b feature/inventory-add-item
```

### Step 3: Do your work and commit

```bash
git add .
git commit -m "feat(inventory): add item form and controller"
```

Commit often, at least every time one small thing works.

### Step 4: Stay up to date with `develop`

If others have merged work while you were coding, bring their changes into your branch **before** opening your PR:

```bash
git fetch origin
git merge origin/develop
```

If Git reports a conflict, see [Fixing merge conflicts](#fixing-merge-conflicts) below.

### Step 5: Check that it builds

```bash
./mvnw clean verify        # Windows: mvnw.cmd clean verify
```

If it fails on your machine, it will fail on GitHub too. Fix it first.

### Step 6: Push your branch

```bash
git push -u origin feature/inventory-add-item
```

### Step 7: Open a Pull Request

1. Go to the repo on GitHub and click **Compare & pull request**.
2. Set **base: `develop`** and **compare: your branch**. Double-check this. It must not be `main`.
3. Fill in the PR template (what you did, how to test it, screenshots for UI changes).
4. Wait for the build check to turn green and for a reviewer to approve.

### Step 8: After the merge

```bash
git checkout develop
git pull origin develop
git branch -d feature/inventory-add-item
```

Then start the next task from Step 2.

## Commit message format

```
<type>(<module>): <short description in present tense>
```

| Type | Use for |
|------|---------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only |
| `style` | Formatting or CSS, no logic change |
| `refactor` | Restructuring code without changing behaviour |
| `chore` | Build, config, housekeeping |

**Good:**
- `feat(auth): add login form validation`
- `fix(cart): correct total when quantity is zero`

**Bad:**
- `update`
- `fixed stuff`
- `final version 2`

## Pull Request rules

- **Every PR needs at least 1 approval** from a teammate before merging.
- Files with a code owner (see `.github/CODEOWNERS`) need that owner's approval. This protects each person's module and the shared code.
- **Reviewers:** read the code, run it if you can, and leave clear comments. Be kind and specific.
- **Authors:** reply to every comment, push fixes to the same branch (the PR updates itself), and don't merge your own PR without approval.
- Prefer **Squash and merge** so `develop` stays tidy (one commit per PR).

## Code standards

- **Packages:** `com.grocery.<module>`, lowercase. Sub-structure: `model`, `repository`, `service`, `controller`.
- **Classes:** `PascalCase` (`InventoryService`). **Methods and variables:** `camelCase` (`addItem`).
- **Constants:** `UPPER_SNAKE_CASE`.
- **Templates:** `src/main/resources/templates/<module>/...`, using the shared `layout.html`.
- **Shared helpers:** use `FileHandler` and `IdGenerator` from `common/`. Don't write your own copies.
- **No hard-coded file paths** or passwords in code.
- Keep methods short, with one job each, and add a short comment where the logic isn't obvious.
- Delete unused code and `System.out.println` debug lines before committing.

## Things you must NOT commit

- `target/` folders, `.idea/` folders, `*.iml` files (already in `.gitignore`)
- Passwords, API keys or personal data
- Large binary files

## Fixing merge conflicts

A conflict means two people changed the same lines. It's normal. Don't panic.

1. Run `git merge origin/develop` and Git lists the conflicted files.
2. Open each one. You'll see:
   ```
   <<<<<<< HEAD
   your version
   =======
   their version
   >>>>>>> origin/develop
   ```
3. Decide what the final code should be (often it's both), delete the `<<<<<<<`, `=======` and `>>>>>>>` lines, and save.
4. Then:
   ```bash
   git add .
   git commit -m "chore: resolve merge conflicts with develop"
   ```
5. If you are unsure, ask the owner of that file or the team leader **before** choosing.

IntelliJ also has a built-in conflict tool (right-click the file, then *Git*, then *Resolve Conflicts*).

## Handy Git commands

| Task | Command |
|------|---------|
| See what changed | `git status` |
| See which branch you're on | `git branch` |
| Undo changes in a file (not yet committed) | `git restore <file>` |
| See recent history | `git log --oneline -10` |
| Switch branch | `git checkout <branch>` |

## Merging into `main` (team leader only)

At each milestone and for the final submission:

1. Make sure `develop` builds and runs with all modules working.
2. Open a PR with **base: `main`** and **compare: `develop`**.
3. Merge with a normal **merge commit** (not squash).
4. Optionally tag the version, e.g. `v0.1-milestone1`.

## Need help?

Ask in the team chat first. Include the exact error message and the command you ran. If you're stuck for more than 30 minutes, ask. That's what the team is for.
