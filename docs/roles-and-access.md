# Roles, login and access (auth + shop + admin modules)

## Default accounts (created automatically when `data/users.txt` is empty)
| Level | Email | Password |
|-------|-------|----------|
| Admin | `admin@grocery.lk` | `Admin@123` |
| Customer | `customer@grocery.lk` | `Customer@123` |

Change these before showing the project to anyone. Public sign-up (`/register`) can only create customers.

## Who can open what (all rules live in `auth/AccessInterceptor.java`)
| Area | Guest | Customer | Admin |
|------|:-----:|:--------:|:-----:|
| `/shop`, `/shop/{id}`, `/login`, `/register` | yes | yes | yes |
| `/cart`, `/orders` (own orders only), checkout | login | yes | yes |
| `/orders/{id}/status` (change status) | no | no | yes |
| `/admin/**`, `/inventory`, `/suppliers`, `/promos`, `/users` | no | no | yes |
| `/reviews`, `/complaints` | login | yes | yes |

A teammate's module is protected automatically by its URL. To protect a new URL, add it to a list at the top of `AccessInterceptor`.

## How other modules know the current user
After login the session holds `userId` (`U001`...), `role` and `userName`. The Order module already reads `userId`.
To get the current user in your own controller: `String userId = AuthSession.userIdOf(session);`

## Packages
| Package | Job |
|---------|-----|
| `auth` | accounts (`Account` -> `CustomerAccount` / `AdminAccount`), login, register, door guard |
| `shop` | customer storefront (`/shop`), reads the inventory |
| `admin` | dashboard, all orders, all accounts, auto-detects which team modules are built |
