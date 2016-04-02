package io.github.mikexliu.api.container;

import com.google.inject.Inject;
import io.github.mikexliu.api.resource.UsersResource;
import io.github.mikexliu.collect.User;
import service.UsersCache;

public class UsersContainer extends UsersResource {

    private final UsersCache usersCache;

    @Inject
    public UsersContainer(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    public String post(final User user) {
        return usersCache.addUser(user);
    }

    @Override
    public User get(String id) {
        return usersCache.getUser(id);
    }
}
