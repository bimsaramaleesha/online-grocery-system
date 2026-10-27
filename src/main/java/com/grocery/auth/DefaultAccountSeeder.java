package com.grocery.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * data/ is not in Git, so on a fresh machine there are no accounts and nobody could log in.
 * When the app starts, this creates a first admin (and a demo customer) if none exist.
 * CHANGE THESE PASSWORDS before showing the project to anyone else.
 */
@Component
public class DefaultAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAccountSeeder.class);

    private final AccountService accounts;

    public DefaultAccountSeeder(AccountService accounts) {
        this.accounts = accounts;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (accounts.countByRole(Role.ADMIN) == 0) {
            accounts.createAdmin("Store Admin", "admin@grocery.lk", "Admin@123", "Store Manager");
            log.info("Created default admin  ->  admin@grocery.lk / Admin@123");
        }
        if (accounts.countByRole(Role.CUSTOMER) == 0) {
            accounts.createDemoCustomer("Demo Customer", "customer@grocery.lk", "Customer@123");
            log.info("Created demo customer ->  customer@grocery.lk / Customer@123");
        }
    }
}
