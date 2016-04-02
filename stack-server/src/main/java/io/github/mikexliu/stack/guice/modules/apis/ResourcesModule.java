package io.github.mikexliu.stack.guice.modules.apis;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import io.github.mikexliu.stack.guice.plugins.stack.StackPlugin;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourcesModule extends StackPlugin {

    private static final Logger log = LoggerFactory.getLogger(ResourcesModule.class);

    private final Map<Method, Method> resourceToContainer;
    private final Collection<String> packageNames;
    private final Injector injector;

    public ResourcesModule(final Collection<String> packageNames, final Injector injector) {
        this.resourceToContainer = new HashMap<>();
        this.packageNames = packageNames;
        this.injector = injector;
    }

    protected void configure() {
        final Set<Class<?>> classes = new HashSet<>();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final String packageName : packageNames) {
                for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClassesRecursive(packageName)) {
                    try {
                        final Class<?> classObject = info.load();
                        if (classObject.isAnnotationPresent(Path.class)) {
                            log.info("Loaded " + classObject);
                            classes.add(classObject);
                        } else if (!Object.class.equals(classObject) && !classObject.isInterface()
                                && classObject.getSuperclass().isAnnotationPresent(Path.class)) {
                            classes.add(classObject);
                        }
                    } catch (NoClassDefFoundError e) {
                        // ignore
                    }
                }
            }

            for (final Class<?> resource : classes) {
                if (Modifier.isAbstract(resource.getModifiers())) {
                    Class<?> container = null;
                    for (final Class<?> clazz : classes) {
                        if (resource.isAssignableFrom(clazz) && resource != clazz) {
                            if (container == null) {
                                container = clazz;
                            } else {
                                throw new IllegalStateException(
                                        "Found multiple implementations of " + resource + " (can only accept one)");
                            }
                        }
                    }
                    if (container == null) {
                        log.warn("Did not find an implementations of " + resource + "; ignoring");
                    } else {
                        bindResourceToLocalContainer(resource, container);
                        log.info("Binding " + resource + " to " + container);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
