package inject;

import resources.Container;
import resources.Item;
import resources.Resource;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class Module<I extends Item> extends AbstractModule {

	private final Class<I> classType;
	private final Class<?> container;
	private final Class<?> resource;

	public Module(final Class<I> classType,
			final Class<? extends Container<I>> container,
			final Class<? extends Resource<I>> resource) {
		this.classType = classType;
		this.container = container;
		this.resource = resource;
	}

	@Override
	protected void configure() {
		bind(classType);
		bind(container).in(Scopes.SINGLETON);
		bind(resource).in(Scopes.SINGLETON);
	}
}
