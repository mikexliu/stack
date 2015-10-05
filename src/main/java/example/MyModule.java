package example;

import inject.StackModule;

public class MyModule extends StackModule<MyResource, MyContainer> {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);
    }
}