package inject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;

import javassist.Modifier;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;

/**
 * Used to bind a Resource to a Container. Only one Resource and Container can
 * be used per StackModule so having multiple end-points will require multiple
 * instances of StackModule.
 * 
 * The canonical use case is calling bindResourceToContainer(resource,
 * container) inside of configure().
 * 
 * @param <?> Resource
 * @param <?> Container
 */
public abstract class StackModule extends AbstractModule {

    @SuppressWarnings("rawtypes")
    private Class resource;
    @SuppressWarnings("rawtypes")
    private Class container;
    private Map<Method, Method> resourceToContainer;

    /**
     * Given a Resource class, binds all common interfaces to the Container.
     * This will make both resource and containers Singletons. class.
     * 
     * @param resource
     * @param container
     */
    protected final void bindResourceToContainer(final Class<?> resource, final Class<?> container) {
        Preconditions.checkArgument(Modifier.isFinal(container.getModifiers()), container
                + " must be declared as final");

        this.resource = resource;
        this.container = container;
        this.resourceToContainer = Maps.newHashMap();

        final Set<Class<?>> resourceInterfaces = Sets.newHashSet(this.resource.getInterfaces());
        final Set<Class<?>> containerInterfaces = Sets.newHashSet(this.container.getInterfaces());
        final Set<Method> intersectingInterfaces = Sets.newHashSet();
        for (final Class<?> intersectingInterface : Sets.intersection(resourceInterfaces, containerInterfaces)) {
            intersectingInterfaces.addAll(Sets.newHashSet(intersectingInterface.getMethods()));
        }

        for (final Method method : intersectingInterfaces) {
            final Method resourceMethod = findMatchingMethod(this.resource, method);
            final Method containerMethod = findMatchingMethod(this.container, method);
            this.resourceToContainer.put(resourceMethod, containerMethod);
        }

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

    @SuppressWarnings("unchecked")
    private final Object bindContainer() {
        try {
            final Object containerInstance = container.newInstance();
            bind(container).toInstance(container.cast(containerInstance));

            return containerInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private final Object bindResource(final Object containerInstance) {
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
                    throw new IllegalAccessException(thisMethod + " is not implemented in "
                            + containerInstance.getClass() + " via interface");
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
