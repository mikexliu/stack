package example.main;

import example.helper.StackServer;

public class Main {

    public static void main(String[] args) throws Exception {
        new StackServer(5555).start(); 
    }
}
