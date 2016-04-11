package io.github.mikexliu.main;

import io.github.mikexliu.api.petstore.v2.user.UserResource;
import io.github.mikexliu.collect.User;
import io.github.mikexliu.stack.client.StackClient;

public class Main {

    public static void main(String[] args) {
        final StackClient stackClient = new StackClient("http", "petstore.swagger.io", 80);
        final UserResource userResource = stackClient.getClient(UserResource.class);

        final User user = new User();
        user.id = 1234;
        user.firstName = "first name";
        user.lastName = "last name";
        user.email = "mxl@github.io";
        user.phone = "123-456-7890";
        user.username = "mxl";
        user.password = "hi";
        user.userStatus = 5;
        userResource.createUser(user);

        User response = userResource.getUserByName("mxl");
        System.out.println(user.firstName.equals(response.firstName)); // true

        user.firstName = "changed name";
        System.out.println(user.firstName.equals(response.firstName)); // false

        userResource.updateUser("mxl", user);
        response = userResource.getUserByName("mxl");
        System.out.println(user.firstName.equals(response.firstName)); // true
    }
}
