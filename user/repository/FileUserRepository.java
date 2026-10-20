package com.grocery.user.repository;

import com.grocery.common.FileHandler;
import com.grocery.common.IdGenerator;
import com.grocery.user.model.User;
import com.grocery.user.model.UserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
public class FileUserRepository implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(FileUserRepository.class);

    private final String fileName;

    public FileUserRepository(@Value("${grocery.data.users-file:users.txt}") String fileName) {
        this.fileName = fileName;
    }

    @Override
    public synchronized List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String line : FileHandler.readLines(fileName)) {
            if (line.startsWith("#")) {
                continue;
            }
            try {
                users.add(UserFactory.fromFileLine(line));
            } catch (RuntimeException e) {
                log.warn("Skipping bad line in {}", fileName);
            }
        }
        return users;
    }

    @Override
    public synchronized Optional<User> findById(String id) {
        return findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    @Override
    public synchronized User save(User user) {
        List<User> users = findAll();
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(nextId(users));
            users.add(user);
        } else {
            boolean replaced = false;
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(user.getId())) {
                    users.set(i, user);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                users.add(user);
            }
        }
        writeAll(users);
        return user;
    }

    @Override
    public synchronized boolean deleteById(String id) {
        List<User> users = findAll();
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            writeAll(users);
        }
        return removed;
    }



    private void writeAll(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.toFileLine());
        }
        FileHandler.writeLines(fileName, lines);
    }

    private String nextId(List<User> users) {
        return IdGenerator.nextId("U", users.stream().map(User::getId).toList());
    }
}
