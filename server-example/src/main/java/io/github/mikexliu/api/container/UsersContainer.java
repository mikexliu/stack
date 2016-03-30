package io.github.mikexliu.api.container;

import com.google.inject.Inject;
import io.github.mikexliu.api.resource.UsersResource;
import service.UsersCache;

public class UsersContainer extends UsersResource {

    private final UsersCache usersCache;

    @Inject
    public UsersContainer(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    public String post(String user) {
        return "Post";
    }

    @Override
    public String get(String id) {
        return id;
    }
}
