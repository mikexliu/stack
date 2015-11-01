package web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.reflections.Reflections;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * 
 */
public class StackModule extends AbstractModule {

    private Map<Method, Method> resourceToContainer;

    protected void configure() {
        final Reflections reflections = new Reflections();
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Path.class);
        for (final Class<?> resource : classes) {
            if (Modifier.isAbstract(resource.getModifiers())) {
                Class<?> containerFound = null;
                for (final Class<?> container : classes) {
                    if (resource.isAssignableFrom(container) && resource != container) {
                        if (containerFound == null) {
                            containerFound = container;
                        } else {
                            throw new IllegalStateException(
                                    "Found multiple implementations of " + resource + " (can only accept one)");
                        }
                    }
                }
                if (containerFound == null) {
                    throw new IllegalStateException("Found no implementations of " + resource);
                } else {
                    bindResourceToContainer(resource, containerFound);
                }
            }
        }
    }

    private final void bindResourceToContainer(final Class<?> resource, final Class<?> container) {
        this.resourceToContainer = Maps.newHashMap();

        // TODO: this should not have non-abstract methods so we should throw exception then
        final Set<Method> abstractMethods = Sets.newHashSet(resource.getMethods()).stream()
                .filter(method -> Modifier.isAbstract(method.getModifiers())).collect(Collectors.toSet());

        for (final Method resourceMethod : abstractMethods) {
            final Method containerMethod = findMatchingMethod(container, resourceMethod);
            if (containerMethod != null) {
                this.resourceToContainer.put(resourceMethod, containerMethod);
            }
        }

        bindResource(bindContainer(container), resource);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Object bindContainer(final Class container) {
        try {
            final Object containerInstance = container.newInstance();
            requestInjection(containerInstance);
            bind(container).toInstance(container.cast(containerInstance));
            return containerInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Object bindResource(final Object containerInstance, Class resource) {
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
                if (containerMethod != null) {
                    return containerMethod.invoke(containerInstance, args);
                } else {
                    throw new IllegalAccessException(
                            thisMethod + " is not implemented in " + containerInstance.getClass() + " via interface");
                }
            }
        };

        try {
            final Object resourceInstance = resource.cast(factory.create(new Class<?>[0], new Object[0], handler));
            bind(resource).toInstance(resource.cast(resourceInstance));
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}