package inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.inject.AbstractModule;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import resources.Container;
import resources.Item;
import resources.Resource;

public class Module<I extends Item, R extends Resource<I>, C extends Container<I>> extends AbstractModule {

    private final Class<I> classType;
    private final Class<R> resource;
    private final Class<C> container;

    public Module(final Class<I> classType, final Class<R> resource, final Class<C> container) {
        this.classType = classType;
        this.resource = resource;
        this.container = container;
    }

    @Override
    protected void configure() {
        bind(classType);

        bindResource(bindContainer());
    }

    private final C bindContainer() {
        C containerInstance;
        try {
            containerInstance = container.newInstance();
            bind(container).toInstance(containerInstance);
            return containerInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private final R bindResource(final C containerInstance) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resource);
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return Modifier.isAbstract(method.getModifiers());
            }
        });

        final MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                System.out.println("Handling " + thisMethod + " via the method handler");
                return null;
            }
        };

        try {
            R resourceInstance = (R) factory.create(new Class<?>[0], new Object[0], handler);
            bind(resource).toInstance(resourceInstance);
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
