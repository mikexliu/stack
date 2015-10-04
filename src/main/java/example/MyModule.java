package example;

import inject.Module;

public class MyModule extends Module<MyResource, MyContainer> {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);
    }
}