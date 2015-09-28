package example;

import com.google.inject.servlet.ServletModule;

import inject.Module;
import web.SwaggerModule;

public class MyModule extends Module<MyResource, MyContainer> {

	public MyModule() {
		super(MyResource.class, MyContainer.class, MyBindings.class);
	}
	
	@Override
	protected void configure() {
	    super.configure();
	    
	    install(new ServletModule());
	    install(new SwaggerModule());
	}
}