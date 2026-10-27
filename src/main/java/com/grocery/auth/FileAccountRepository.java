package com.grocery.auth;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores accounts in data/users.txt, one per line. Same read-all / change / write-all rule as items. */
@Repository
public class FileAccountRepository implements AccountRepository {

    private static final Logger log = LoggerFactory.getLogger(FileAccountRepository.class);

    private final String fileName;

    public FileAccountRepository(@Value("${grocery.data.users-file:users.txt}") String fileName) {
        this.fileName = fileName;
    }

    @Override
    public synchronized List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        for (String line : FileHandler.readLines(fileName)) {
            try {
                accounts.add(AccountFactory.fromFileLine(line));
            } catch (RuntimeException e) {      // one bad line must not break the login page
                log.warn("Skipping bad line in {}: {}", fileName, line);
            }
        }
        return accounts;
    }

    @Override
    public synchronized Optional<Account> findById(String id) {
        return findAll().stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    @Override
    public synchronized Optional<Account> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String wanted = email.trim().toLowerCase();
        return findAll().stream().filter(a -> a.getEmail().equals(wanted)).findFirst();
    }

    @Override
    public synchronized Account save(Account account) {
        List<Account> accounts = findAll();
        if (account.getId() == null || account.getId().isBlank()) {
            account.setId(IdGenerator.nextId("U", accounts.stream().map(Account::getId).toList()));   // CREATE
            accounts.add(account);
        } else {                                                                                       // UPDATE
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getId().equals(account.getId())) {
                    accounts.set(i, account);
                    break;
                }
            }
        }
        FileHandler.writeLines(fileName, accounts.stream().map(Account::toFileLine).toList());
        return account;
    }
}
