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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Getter(AccessLevel.PACKAGE)
public class Component {

    @Getter
    private final String name;
    @NotNull
    protected final File jarFile;
    @NotNull
    protected final ClassLoader classLoader;
    @Nullable
    protected Method startMethod;
    @Nullable
    protected Method stopMethod;

    @Setter(value = AccessLevel.PACKAGE)
    private AbstractComponentState componentState;

    public Component(File jarFile) throws MalformedURLException {
        if (!jarFile.getName().endsWith(".jar")){
            throw new IllegalArgumentException(jarFile.getName() + " is not a jar file.");
        }
        this.name = Component.getNameFromJarFile(jarFile);
        this.jarFile = jarFile;
        this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
        componentState = new ComponentLoaded();
    }

    public Component(Component component) throws MalformedURLException {
        this(component.jarFile);
    }

    public static String getNameFromJarFile(File jarFile){
        if (!jarFile.getName().endsWith(".jar")){
            throw new IllegalArgumentException(jarFile.getName() + " is not a jar file.");
        }
        return jarFile.getName().substring(0, jarFile.getName().length() - 4);
    }

    public void deploy(RuntimeEnvironment re) throws Exception {
        Method[] methods = componentState.deploy(this, re);
        if (methods != null){
            startMethod = methods[0];
            stopMethod = methods[1];
        }
        componentState = new ComponentDeployed();
    }

    public void start() throws Exception {
        componentState.start(this);
        componentState = new ComponentStarted();
    }

    public void stop() throws Exception{
        componentState.stop(this);
        componentState = new ComponentStopped();
    }

    public void delete(){
        componentState.delete(this);
    }

    public ComponentState getState(){
        return componentState.getState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Component component = (Component) o;

        if (!Objects.equals(name, component.name)) return false;
        if (!Objects.equals(jarFile, component.jarFile)) return false;
        if (!Objects.equals(classLoader, component.classLoader))
            return false;
        if (!Objects.equals(startMethod, component.startMethod))
            return false;
        return Objects.equals(stopMethod, component.stopMethod);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (jarFile != null ? jarFile.hashCode() : 0);
        result = 31 * result + (classLoader != null ? classLoader.hashCode() : 0);
        result = 31 * result + (startMethod != null ? startMethod.hashCode() : 0);
        result = 31 * result + (stopMethod != null ? stopMethod.hashCode() : 0);
        return result;
    }
}
