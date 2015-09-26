package example;

import inject.Module;

public class MyModule extends Module<MyItem> {

	public MyModule() {
		super(MyItem.class, MyContainer.class, MyResource.class);
	}
}
