package service;

import com.google.common.collect.ImmutableMap;
import io.github.mikexliu.collect.User;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, User> getAllUsers() {
        return ImmutableMap.copyOf(users);
    }
}
