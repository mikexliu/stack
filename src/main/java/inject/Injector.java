package inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import resources.Resource;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverterBinding;

public final class Injector implements com.google.inject.Injector {

    private final com.google.inject.Injector injector;

    // cache custom resource objects
    private final Map<Object, Object> resourceBindings;

    public Injector(final com.google.inject.Module... modules) {
        injector = Guice.createInjector(modules);
        resourceBindings = new HashMap<Object, Object>();
    }

    @Override
    public void injectMembers(Object instance) {
        injector.injectMembers(instance);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return injector.getMembersInjector(typeLiteral);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return injector.getMembersInjector(type);
    }

    @Override
    public Map<Key<?>, Binding<?>> getBindings() {
        return injector.getBindings();
    }

    @Override
    public Map<Key<?>, Binding<?>> getAllBindings() {
        return injector.getAllBindings();
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key) {
        return injector.getBinding(key);
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {
        return injector.getBinding(type);
    }

    @Override
    public <T> Binding<T> getExistingBinding(Key<T> key) {
        return injector.getExistingBinding(key);
    }

    @Override
    public <T> List<Binding<T>> findBindingsByType(TypeLiteral<T> type) {
        return injector.findBindingsByType(type);
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return injector.getProvider(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return injector.getProvider(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(Key<T> key) {
        if (Resource.class.isAssignableFrom(key.getTypeLiteral().getRawType()) && resourceBindings.containsKey(key)) {
            return (T) resourceBindings.get(key);
        } else if (Resource.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
            final T object = (T) mapResourceToContainer((Resource<?>) injector.getInstance(key));
            resourceBindings.put(key, object);
            resourceBindings.put(key.getTypeLiteral().getRawType(), object);
            return object;
        }

        final T object = injector.getInstance(key);
        return object;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(Class<T> type) {
        if (Resource.class.isAssignableFrom(type) && resourceBindings.containsKey(type)) {
            return (T) resourceBindings.get(type);
        } else if (Resource.class.isAssignableFrom(type)) {
            final T object = (T) mapResourceToContainer((Resource<?>) injector.getInstance(type));
            resourceBindings.put(type, object);
            resourceBindings.put(Key.get(type), object);
            return object;
        }

        final T object = injector.getInstance(type);
        return object;
    }

    @Override
    public com.google.inject.Injector getParent() {
        return injector.getParent();
    }

    @Override
    public com.google.inject.Injector createChildInjector(Iterable<? extends com.google.inject.Module> modules) {
        return injector.createChildInjector(modules);
    }

    @Override
    public com.google.inject.Injector createChildInjector(com.google.inject.Module... modules) {
        return injector.createChildInjector(modules);
    }

    @Override
    public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
        return injector.getScopeBindings();
    }

    @Override
    public Set<TypeConverterBinding> getTypeConverterBindings() {
        return injector.getTypeConverterBindings();
    }

    /**
     * 
     * @param resource
     * @return
     */
    private Resource<?> mapResourceToContainer(final Resource<?> resource) {
        final Class<?> classType = resource.getClass();

        final Set<Method> methods = getLocallyDeclaredMethods(classType);
        for (Method method : methods) {
            System.out.println(method);
        }
        
        // override each method to call the container's corresponding code

        return resource;
    }

    /**
     * Get all declared Methods for a given class. This will only return the
     * methods explicitly declared. Parent methods are ignored.
     * 
     * @param classType
     * @return
     */
    private Set<Method> getLocallyDeclaredMethods(final Class<?> classType) {
        final Collection<Method> classTypeMethods = Arrays.asList(classType.getDeclaredMethods());
        final Collection<Method> superClassTypeMethods = Arrays.asList(classType.getSuperclass().getMethods());

        final Map<String, Set<Method>> methodNameToMethod = new HashMap<>();
        for (final Method method : classTypeMethods) {
            final String methodName = method.getName();
            if (!methodNameToMethod.containsKey(methodName)) {
                methodNameToMethod.put(methodName, new HashSet<>());
            }

            final Set<Method> methodSet = methodNameToMethod.get(methodName);
            methodSet.add(method);
        }

        for (final Entry<String, Set<Method>> entry : methodNameToMethod.entrySet()) {
            if (entry.getValue().size() <= 1) {
                continue;
            }

            final Set<Method> filteredMethodSet = entry.getValue().stream().filter(new Predicate<Method>() {

                @Override
                public boolean test(Method method) {
                    for (final Method superMethod : superClassTypeMethods) {
                        if (Arrays.asList(method.getParameterTypes()).equals(
                                Arrays.asList(superMethod.getParameterTypes()))
                                && method.getReturnType().equals(superMethod.getReturnType())) {
                            return false;
                        }
                    }
                    return true;
                }
            }).collect(Collectors.toSet());
            entry.setValue(filteredMethodSet);
        }

        final Set<Method> allMethods = new HashSet<>();
        methodNameToMethod.values().forEach(allMethods::addAll);

        return allMethods;
    }
}
