package io.github.mikexliu.stack.guice.modules.apis;

import com.google.common.reflect.ClassPath;
import com.google.inject.Scopes;
import io.github.mikexliu.stack.guice.plugins.front.FrontModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class ContainersModule extends FrontModule {

    private static final Logger log = LoggerFactory.getLogger(ContainersModule.class);

    private final Collection<String> packageNames;

    public ContainersModule(final Collection<String> packageNames) {
        this.packageNames = packageNames;
    }

    protected void configure() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final String packageName : packageNames) {
                for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClassesRecursive(packageName)) {
                    try {
                        final Class<?> classObject = info.load();
                        if (!Object.class.equals(classObject)
                                && !classObject.isInterface()
                                && !Modifier.isAbstract(classObject.getModifiers())
                                && classObject.getSuperclass().isAnnotationPresent(Path.class)) {
                            log.info("Binding " + classObject + " in " + Scopes.SINGLETON);
                            bind(classObject).in(Scopes.SINGLETON);
                        }
                    } catch (NoClassDefFoundError e) {
                        // ignore
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
