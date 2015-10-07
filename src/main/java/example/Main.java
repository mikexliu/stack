package example;

import web.Stack;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {

    public static void main(String[] args) {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        new Stack(injector).start();
    }
}
