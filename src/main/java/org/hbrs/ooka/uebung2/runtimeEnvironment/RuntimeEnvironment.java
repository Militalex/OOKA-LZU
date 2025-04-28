package org.hbrs.ooka.uebung2.runtimeEnvironment;

import lombok.Getter;
import org.hbrs.ooka.uebung2.component.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

public class RuntimeEnvironment {

    public static final Logger LOGGER = Logger.getLogger(RuntimeEnvironment.class.getName());

    @Getter
    private final RuntimeEnvironmentAPI api = new RuntimeEnvironmentAPI();

    @NotNull
    private final Path compDir;

    @Getter
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
        LOGGER.info("Starte Laufzeitumgebung ...");
        if (isRunning()){
            LOGGER.severe("Laufzeitumgebung ist bereits gestartet.");
            return;
        }
        Arrays.stream(Objects.requireNonNull(compDir.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".jar")).forEach(file -> {
            Component component = new Component(file);
            components.put(component.getName(), component);
        });

        running = true;
        LOGGER.info("- - - Laufzeitumgebung erfolgreich gestartet - - -");
    }

    public void shutdown(){
        LOGGER.info("Fahre Laufzeitumgang herunter ...");
        if (!isRunning()){
            LOGGER.severe("Laufzeitumgebung ist bereits beendet.");
            return;
        }

        running = false;
        components.clear();
        LOGGER.info("- - - Laufzeitumgebung erfolgreich heruntergefahren - - -");
    }
}
