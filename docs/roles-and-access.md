# Roles, login and access (user + shop + admin modules)

## Default accounts (created automatically when `data/users.txt` is empty)
| Level | Email | Password |
|-------|-------|----------|
| Admin | `admin@grocery.lk` | `Admin@123` |
| Customer | `customer@grocery.lk` | `Customer@123` |

Change these before showing the project to anyone. Public sign-up (`/register`) can only create customers.

## Who can open what (all rules live in `user/controller/SessionAuth.java`)
| Area | Guest (New Customer) | Existing Customer | Admin |
|------|:--------------------:|:-----------------:|:-----:|
| `/shop`, `/shop/{id}`, `/login`, `/register` | yes | yes | yes |
| `/cart`, `/orders` (own orders only), checkout | login | yes | yes |
| `/users/profile` (own profile, change password, delete account) | login | yes | yes |
| `/orders/{id}/status` (change status) | no | no | yes |
| `/admin/**`, `/inventory`, `/suppliers`, `/promos`, `/users` | no | no | yes |
| `/reviews`, `/complaints` | login | yes | yes |

A teammate's module is protected automatically by its URL. To protect a new URL, add it to a list at the top of `SessionAuth`.

## How other modules know the current user
After login (`user/controller/AuthController`) the session holds `userId` (`U001`...), `role` and `userName`. The Order module already reads `userId`.
To get the current user in your own controller: `String userId = SessionAuth.userIdOf(session);`

## Packages
| Package | Job |
|---------|-----|
| `user` | all user tasks: new customer registration, login, existing customer profile, admin user management, and request door guard (`SessionAuth`) |
| `shop` | customer storefront (`/shop`), reads the inventory |
| `admin` | dashboard, all orders, auto-detects which team modules are built |
