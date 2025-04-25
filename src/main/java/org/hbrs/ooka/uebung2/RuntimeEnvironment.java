package org.hbrs.ooka.uebung2;

import lombok.Getter;
import lombok.Setter;
import org.hbrs.ooka.uebung2.component.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class RuntimeEnvironment {

    @NotNull
    @Setter @Getter
    private Path cmpDir;

    private HashMap<String, Component> components = new HashMap<>();

    public RuntimeEnvironment() {
        this("cmps");
    }

    public RuntimeEnvironment(String cmpDir) {
        Path cmpPath = Paths.get(cmpDir);
        if (!cmpPath.toFile().isDirectory()){
            throw new IllegalArgumentException(cmpDir + " is not a directory");
        }
        this.cmpDir = cmpPath;
    }

    public void start(){

    }

    public void shutdown(){

    }
}
