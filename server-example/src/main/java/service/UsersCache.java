package service;

import io.github.mikexliu.collect.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mliu on 3/28/16.
 */
public class UsersCache {

    private final Map<String, User> users;

    public UsersCache() {
        this.users = new HashMap<>();
    }

    public String addUser(final User user) {
        final String id = Integer.toHexString(user.hashCode());
        users.put(id, user);
        return id;
    }

    public User getUser(final String id) {
        return users.get(id);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }
}
