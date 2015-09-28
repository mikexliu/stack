package inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import resources.Container;
import resources.Resource;

public class Module<R extends Resource, C extends Container> extends AbstractModule {

    private final Class<R> resource;
    private final Class<C> container;
    private final Map<Method, Method> resourceToContainer;

    public Module(final Class<R> resource, final Class<C> container,
            final Class<?> bindings) {
        Preconditions.checkArgument(bindings.isInterface());
        Preconditions.checkArgument(bindings.isAssignableFrom(resource));
        Preconditions.checkArgument(bindings.isAssignableFrom(container));

        this.resource = resource;
        this.container = container;
        this.resourceToContainer = Maps.newHashMap();

        for (final Method method : bindings.getMethods()) {
            final Method resourceMethod = findMatchingMethod(this.resource, method);
            final Method containerMethod = findMatchingMethod(this.container, method);
            Preconditions.checkArgument(resourceMethod != null, this.resource + " did not implement " + method);
            Preconditions.checkArgument(containerMethod != null, this.container + " did not implement " + method);
            this.resourceToContainer.put(resourceMethod, containerMethod);
        }
    }

    @Override
    protected void configure() {
        bindResource(bindContainer());
    }

    private final Method findMatchingMethod(final Class<?> classType, final Method matchingMethod) {
        for (final Method method : classType.getMethods()) {
            if (method.getName().equals(matchingMethod.getName())
                    && matchParameters(method.getParameters(), matchingMethod.getParameters())) {
                return method;
            }
        }
        return null;
    }
    
    private final boolean matchParameters(final Parameter[] parameters1, final Parameter[] parameters2) {
        if (parameters1.length != parameters2.length) {
            return false;
        } else {
            for (int i = 0; i < parameters1.length; i++) {
                final Parameter parameter1 = parameters1[i];
                final Parameter parameter2 = parameters2[i];
                
                if (!parameter1.getType().equals(parameter2.getType())) {
                    return false;
                }
            }
            return true;
        }
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
            public Object invoke(Object b, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                final Method containerMethod = resourceToContainer.get(thisMethod);
                return containerMethod.invoke(containerInstance, args);
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
