package io.github.stack.guice.modules.apis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourcesModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ResourcesModule.class);

    private final Map<Method, Method> resourceToContainer;
    private final Injector injector;

    public ResourcesModule(final Injector injector) {
        this.resourceToContainer = new HashMap<>();
        this.injector = injector;
    }

    protected void configure() {
        final Set<Class<?>> resources = new HashSet<>();
        final Map<Class<?>, Class<?>> resourcesWithContainers = new HashMap<>();
        injector.getAllBindings().entrySet().forEach(entry -> {
            final Key<?> key = entry.getKey();
            final Class<?> classObject = key.getTypeLiteral().getRawType();
            final Class<?> superClassObject = classObject.getSuperclass();
            if (classObject.isAnnotationPresent(Path.class)) {
                log.info("Loaded Resource " + classObject);
                resources.add(classObject);
            } else if (!Object.class.equals(classObject) && !classObject.isInterface()
                    && superClassObject.isAnnotationPresent(Path.class)) {
                log.info("Loaded Resource " + superClassObject + " with Container " + classObject);
                resourcesWithContainers.put(superClassObject, classObject);
            }
        });

        for (final Class<?> resource : resources) {
            final Object container = injector.getInstance(resource);
            bind((Class) resource).toInstance(container);
            log.info("Binding Resource " + resource + " to Container " + container);
        }

        for (final Map.Entry<Class<?>, Class<?>> entry : resourcesWithContainers.entrySet()) {
            bindResourceToLocalContainer(entry.getKey(), entry.getValue());
            log.info("Binding Resource " + entry.getKey() + " to Container " + entry.getValue());
        }
    }

    private final Method findMatchingMethod(final Class<?> classType, final Method matchingMethod) {
        for (final Method method : classType.getMethods()) {
            if (method.getName().equals(matchingMethod.getName())
                    && method.getReturnType().equals(matchingMethod.getReturnType())
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

    private final void bindResourceToLocalContainer(final Class<?> resource, final Class<?> container) {
        final Set<Method> nonAbstractMethods = Sets.newHashSet(resource.getMethods()).stream()
                .filter(method -> !Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toSet());
        Preconditions.checkState(!nonAbstractMethods.isEmpty(), "Found non-abstract methods in " + resource + ": " + nonAbstractMethods);

        final Set<Method> abstractMethods = Sets.newHashSet(resource.getMethods()).stream()
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toSet());

        for (final Method resourceMethod : abstractMethods) {
            final Method containerMethod = findMatchingMethod(container, resourceMethod);
            if (containerMethod != null) {
                this.resourceToContainer.put(resourceMethod, containerMethod);
            }
        }

        bindResourceToContainer(resource, injector.getInstance(container));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final Object bindResourceToContainer(final Class resource, final Object containerInstance) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resource);
        factory.setFilter(method -> Modifier.isAbstract(method.getModifiers()));

        final MethodHandler handler = (b, thisMethod, proceed, args) -> {
            final Method containerMethod = resourceToContainer.get(thisMethod);
            if (containerMethod != null) {
                return containerMethod.invoke(containerInstance, args);
            } else {
                throw new IllegalAccessException(
                        thisMethod + " is not implemented in " + containerInstance.getClass() + " via interface");
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
