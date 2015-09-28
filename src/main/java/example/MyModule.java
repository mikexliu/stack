package example;

import inject.Module;

public class MyModule extends Module<MyItem, MyResource, MyContainer> {

	public MyModule() {
		super(MyItem.class, MyResource.class, MyContainer.class);
	}
}