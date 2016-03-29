package collect;

import java.util.Date;

/**
 * Created by mliu on 3/28/16.
 */
public class User {

    private String username;
    private Date age;

    public User(String username) {
        this.username = username;
        this.age = new Date();
    }
}
