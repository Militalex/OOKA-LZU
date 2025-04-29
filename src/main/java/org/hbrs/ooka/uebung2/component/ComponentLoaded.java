package org.hbrs.ooka.uebung2.component;

import org.hbrs.ooka.uebung2.runtimeEnvironment.RuntimeEnvironment;
import org.hbrs.ooka.uebung2.services.logger.ComponentLogger;
import org.hbrs.ooka.uebung3.annotations.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentLoaded extends AbstractComponentState {

    @Override
    public @Nullable Method[] deploy(Component component, RuntimeEnvironment re, int id) throws Exception{
        Enumeration<JarEntry> entryEnumeration = new JarFile(component.getJarFile()).entries();
        Method[] methods = null;

        while (entryEnumeration.hasMoreElements()) {
            JarEntry entry = entryEnumeration.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class") || entry.getName().startsWith("META-INF") || entry.getName().startsWith("test")) continue;
            String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);
            final Class<?> clazz = component.getClassLoader().loadClass(className);

            // Calculate port class
            if (clazz.isAnnotationPresent(Port.class)) {
                re.getApi().addPort(component, clazz.getDeclaredConstructor().newInstance());
            }

            // Calculate start and stop methods
            Method[] startAndStopMethods = getStartAndStopMethodsIfPresent(component, clazz);
            if (startAndStopMethods != null) {
                if (methods != null) {
                    RuntimeEnvironment.LOGGER.warning("Mehr als eine Startklasse in der Komponente " + component.getName() +
                            " gefunden. Belasse zuletzt gefundene Startklasse " + clazz.getName());
                }
                else {
                    methods = startAndStopMethods;

                    // Inject
                    Arrays.stream(clazz.getFields()).filter(field -> field.isAnnotationPresent(Inject.class)).forEach(field -> {
                        field.setAccessible(true);

                        try {
                            switch (field.getAnnotation(Inject.class).injectType()) {
                                case RUNTIME_ENVIRONMENT -> field.set(null, re.getApi());
                                case LOGGER -> field.set(null, new ComponentLogger(component, id));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
        return methods;
    }

    private @Nullable Method[] getStartAndStopMethodsIfPresent(Component component, Class<?> clazz){
        Method startMethod = null;
        Method stopMethod = null;
        for (int i = 0; i < clazz.getMethods().length && (startMethod == null || stopMethod == null); i++) {
            Method method = clazz.getMethods()[i];

            if (method.isAnnotationPresent(Start.class)) {
                if (startMethod != null) {
                    RuntimeEnvironment.LOGGER.severe("Mehr als eine Startmethode in der Komponente " + component.getName() + " gefunden.");
                    return null;
                }

                startMethod = method;
            }

            if (method.isAnnotationPresent(Stop.class)) {
                if (stopMethod != null) {
                    RuntimeEnvironment.LOGGER.severe("Mehr als eine Stopmethode in der Komponente " + component.getName() + " gefunden.");
                    return null;
                }

                stopMethod = method;
            }
        }

        if (startMethod == null && stopMethod == null){
            return null;
        }

        else if (startMethod == null){
            RuntimeEnvironment.LOGGER.severe("Keine Startmethode in der Komponente " + component.getName() + " gefunden, obwohl eine Stopmethode vorhanden ist.");
            return null;
        }

        else if (stopMethod == null){
            RuntimeEnvironment.LOGGER.severe("Keine Stopmethode in der Komponente " + component.getName() + " gefunden, obwohl eine Startmethode vorhanden ist.");
            return null;
        }

        return new Method[]{startMethod, stopMethod};
    }

    @Override
    public ComponentState getState() {
        return ComponentState.LOADED;
    }
}
