package org.hbrs.ooka.uebung2_3.component;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hbrs.ooka.uebung2_3.runtimeEnvironment.RuntimeEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Objects;

@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PACKAGE)
public class Component {

    @Getter
    private final String name;
    @Getter
    private final String fullName;
    @NotNull
    protected final File jarFile;
    @NotNull @Getter
    protected final ClassLoader classLoader;
    @Nullable
    protected Method startMethod;
    @Nullable
    protected Method stopMethod;
    @Nullable
    private Thread thread;

    @Setter(value = AccessLevel.PACKAGE)
    private AbstractComponentState componentState;

    public Component(String path) throws MalformedURLException {
        File fileToJar = Paths.get(path).toFile();
        if (!fileToJar.getName().endsWith(".jar")){
            throw new IllegalArgumentException(fileToJar.getName() + " is not a jar file.");
        }
        this.name = Component.getNameFromJarFile(fileToJar);
        this.fullName = path;
        this.jarFile = fileToJar;
        this.classLoader = new URLClassLoader(new URL[]{fileToJar.toURI().toURL()});
        componentState = new ComponentLoaded();
    }

    public Component(Component component) throws MalformedURLException {
        this(component.fullName);
    }

    public static String getNameFromJarFile(File jarFile){
        if (!jarFile.getName().endsWith(".jar")){
            throw new IllegalArgumentException(jarFile.getName() + " is not a jar file.");
        }
        return jarFile.getName().substring(0, jarFile.getName().length() - 4);
    }

    public void deploy(RuntimeEnvironment re, int id) throws Exception {
        Method[] methods = componentState.deploy(this, re, id);
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
        componentState.stop(this, thread);
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
