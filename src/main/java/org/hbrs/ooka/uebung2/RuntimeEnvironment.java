package org.hbrs.ooka.uebung2;

import lombok.Getter;
import lombok.Setter;
import org.hbrs.ooka.uebung2.component.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class RuntimeEnvironment {

    private final Logger logger = Logger.getLogger(RuntimeEnvironment.class.getName());

    @NotNull @Getter
    private final Path compDir;

    private HashMap<String, Component> components = new HashMap<>();

    @Getter
    private boolean running = false;

    public RuntimeEnvironment(String compDir) {
        Path cmpPath = Paths.get(compDir);
        if (!cmpPath.toFile().isDirectory()){
            throw new IllegalArgumentException(compDir + " is not a directory");
        }
        this.compDir = cmpPath;
    }

    public void start(){
        if (isRunning()){
            logger.severe("RuntimeEnvironment is already running.");
            return;
        }
        Arrays.stream(Objects.requireNonNull(compDir.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".jar")).forEach(file -> {
            Component component = new Component(file);
            components.put(component.getName(), component);
        });

        running = true;
    }

    public void shutdown(){
        if (!isRunning()){
            logger.severe("RuntimeEnvironment is not running.");
            return;
        }

        running = false;
        components.clear();
    }
}
