package service;

import collect.User;

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

    public String addUser(final String username) {
        final String id = Integer.toHexString(username.hashCode());
        final User user = new User(username);
        users.put(id, user);
        return id;
    }

    public User getUser(final String id) {
        return users.get(id);
    }
}
