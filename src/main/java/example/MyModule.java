package example;

import inject.Module;

public class MyModule extends Module<MyResource, MyContainer> {

	public MyModule() {
		super(MyResource.class, MyContainer.class, MyBindings.class);
	}
}