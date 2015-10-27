package example;

import com.google.inject.Guice;
import com.google.inject.Injector;

import web.Stack;

public class Main {

    public static void main(String[] args) {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        new Stack(injector).start();
    }
}
