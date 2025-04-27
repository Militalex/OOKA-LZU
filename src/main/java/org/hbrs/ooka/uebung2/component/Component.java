package org.hbrs.ooka.uebung2.component;

import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class Component {

    private static int next_id = 1;

    @Getter
    private final int id;

    @Getter
    private final String name;

    private final JarFile jarFile;
    private final ClassLoader classLoader;

    private ComponentState componentState;

    @SneakyThrows
    public Component(File jarFile){
        if (!jarFile.getName().endsWith(".jar")){
            throw new IllegalArgumentException(jarFile.getName() + " is not a jar file.");
        }

        this.id = next_id++;
        this.name = jarFile.getName().substring(0, jarFile.getName().length() - 4);
        this.jarFile = new JarFile(jarFile, false, ZipFile.OPEN_READ, Runtime.version());
        this.classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
    }
}
