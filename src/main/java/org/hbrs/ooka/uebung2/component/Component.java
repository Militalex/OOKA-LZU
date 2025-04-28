package org.hbrs.ooka.uebung2.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hbrs.ooka.uebung2.runtimeEnvironment.IRuntimeEnvironmentAPI;
import org.hbrs.ooka.uebung2.runtimeEnvironment.RuntimeEnvironment;
import org.hbrs.ooka.uebung2.annotations.Port;
import org.hbrs.ooka.uebung2.annotations.Start;
import org.hbrs.ooka.uebung2.annotations.Stop;
import org.hbrs.ooka.uebung2.util.Reflector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Component {

    private static int next_id = 1;

    @Getter
    private final int id;

    @Getter
    private final String name;

    private final JarFile jarFile;

    private final ClassLoader classLoader;

    @Nullable
    private Method startMethod;

    @Nullable
    private Method stopMethod;

    @Setter(value = AccessLevel.PACKAGE)
    private ComponentState componentState;

    @SneakyThrows
    public Component(File jarFile) {
        if (!jarFile.getName().endsWith(".jar")){
            throw new IllegalArgumentException(jarFile.getName() + " is not a jar file.");
        }

        this.id = next_id++;
        this.name = jarFile.getName().substring(0, jarFile.getName().length() - 4);
        this.jarFile = new JarFile(jarFile);
        this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
    }

    public void deploy(RuntimeEnvironment re) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Enumeration<JarEntry> entryEnumeration = jarFile.entries();

        while (entryEnumeration.hasMoreElements()) {
            JarEntry entry = entryEnumeration.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class") || entry.getName().startsWith("META-INF") || entry.getName().startsWith("test")) continue;
            String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);
            final Class<?> clazz = classLoader.loadClass(className);

            // Calculate port class
            if (clazz.isAnnotationPresent(Port.class)) {
                re.getApi().addPort(clazz.getDeclaredConstructor().newInstance());
            }

            // Calculate start and stop methods
            Method[] startAndStopMethods = getStartAndStopMethodsIfPresent(clazz);
            if (startAndStopMethods != null) {
                if (startMethod != null || stopMethod != null) {
                    RuntimeEnvironment.LOGGER.warning("Mehr als eine Startklasse in der Komponente " + name +
                            " gefunden. Belasse zuletzt gefundene Startklasse " + clazz.getName());
                }
                else {
                    startMethod = startAndStopMethods[0];
                    stopMethod = startAndStopMethods[1];

                    // Set API
                    Arrays.stream(clazz.getFields()).filter(field -> field.getType().equals(IRuntimeEnvironmentAPI.class)).forEach(field -> {
                        try {
                            field.setAccessible(true);
                            field.set(null, re.getApi());
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }

    private @Nullable Method[] getStartAndStopMethodsIfPresent(Class<?> clazz){
        Method startMethod = null;
        Method stopMethod = null;
        for (int i = 0; i < clazz.getMethods().length && (startMethod == null || stopMethod == null); i++) {
            Method method = clazz.getMethods()[i];

            if (method.isAnnotationPresent(Start.class)) {
                if (startMethod != null) {
                    RuntimeEnvironment.LOGGER.severe("Mehr als eine Startmethode in der Komponente " + name + " gefunden.");
                    return null;
                }

                startMethod = method;
            }

            if (method.isAnnotationPresent(Stop.class)) {
                if (stopMethod != null) {
                    RuntimeEnvironment.LOGGER.severe("Mehr als eine Stopmethode in der Komponente " + name + " gefunden.");
                    return null;
                }

                stopMethod = method;
            }
        }

        if (startMethod == null && stopMethod == null){
            return null;
        }

        else if (startMethod == null){
            RuntimeEnvironment.LOGGER.severe("Keine Startmethode in der Komponente " + name + " gefunden, obwohl eine Stopmethode vorhanden ist.");
            return null;
        }

        else if (stopMethod == null){
            RuntimeEnvironment.LOGGER.severe("Keine Stopmethode in der Komponente " + name + " gefunden, obwohl eine Startmethode vorhanden ist.");
            return null;
        }

        return new Method[]{startMethod, stopMethod};
    }

    @SneakyThrows
    public void start() {
        if (startMethod != null) {
            startMethod.setAccessible(true);
            startMethod.invoke(null);
        }
    }

    @SneakyThrows
    public void stop(){
        if (stopMethod != null) {
            stopMethod.setAccessible(true);
            stopMethod.invoke(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Component component)) return false;

        if (id != component.id) return false;
        if (!name.equals(component.name)) return false;
        if (!jarFile.equals(component.jarFile)) return false;
        if (!classLoader.equals(component.classLoader)) return false;
        if (!Objects.equals(startMethod, component.startMethod))
            return false;
        return Objects.equals(stopMethod, component.stopMethod);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }
}
