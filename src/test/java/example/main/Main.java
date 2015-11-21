package example.main;

import example.helper.StackServerHelper;

public class Main {

    public static void main(String[] args) throws Exception {
        new StackServerHelper(5555).start();
    }
}
