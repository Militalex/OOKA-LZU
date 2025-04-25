package org.hbrs.ooka.uebung2.component;

import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class Component {

    private static int next_id = 1;

    @Getter
    private final int id;

    @Getter
    private final String name;

    @Nullable @Getter
    private final ClassLoader classLoader;

    private ComponentState componentState;

    @SneakyThrows
    public Component(Path jarFile){
        this.id = next_id++;
        this.name = jarFile.toString(); // Check
        this.classLoader = new URLClassLoader(new URL[]{jarFile.toUri().toURL()});

    }
}
