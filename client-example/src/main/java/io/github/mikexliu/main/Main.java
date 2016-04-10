package io.github.mikexliu.main;

import io.github.mikexliu.api.users.v1.resource.UsersResource;
import io.github.mikexliu.collect.User;
import io.github.mikexliu.stack.client.StackClient;

public class Main {

    public static void main(String[] args) {
        final StackClient stackClient = new StackClient("http", "localhost", 5454);
        final UsersResource usersResource = stackClient.getClient(UsersResource.class);

        final User user = new User();
        user.name = "new user";
        user.age = 12;
        final String id = usersResource.post(user);

        final User returnedUser = usersResource.get(id);

        System.out.println(user.name.equals(returnedUser.name)); // true
    }
}
