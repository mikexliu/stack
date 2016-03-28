package io.github.mikexliu.api.container;

import io.github.mikexliu.api.resource.UsersResource;

public class UsersContainer extends UsersResource {

    @Override
    public String post(String user) {
        return "Post";
    }

    @Override
    public String get(String id) {
        return id;
    }
}
