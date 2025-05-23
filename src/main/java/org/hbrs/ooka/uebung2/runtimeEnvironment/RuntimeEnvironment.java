package org.hbrs.ooka.uebung2.runtimeEnvironment;

import lombok.Getter;
import org.hbrs.ooka.uebung2.component.Component;
import org.hbrs.ooka.uebung2.component.ComponentState;
import org.hbrs.ooka.uebung2.util.LoggerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RuntimeEnvironment {
    public static final Logger LOGGER = LoggerUtil.getLogger(RuntimeEnvironment.class);
    @Getter
    private final RuntimeEnvironmentAPI api = new RuntimeEnvironmentAPI();
    @NotNull
    private final Path compDir;

    /**
     * Die Laufende Identifikationsnummer ergibt sich als die Position in der ArrayList.
     */
    private ArrayList<Component> components = new ArrayList<>();
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
                    Component component = null;
                    try {
                        component = new Component(file);
                        components.add(component);
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "Komponente mit dem Dateinamen \"" + file.getName() +
                                "\" konnte nicht geladen werden aufgrund eines Fehlers: Siehe folgende Fehlermeldung: ", e);
                    }
        });

        running = true;
        LOGGER.info("- - - Laufzeitumgebung erfolgreich gestartet - - -");
    }

    public void refresh(){
        if (!isRunning()){
            LOGGER.warning("Laufzeitumgebung ist noch nicht gestartet worden.");
            return;
        }

        List<String> compNames = components.stream().map(Component::getName).toList();
        StringBuilder addedComponents = new StringBuilder();

        Arrays.stream(Objects.requireNonNull(compDir.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".jar"))
                .filter(file -> !compNames.contains(Component.getNameFromJarFile(file)))
                .forEach(file -> {
                    Component component;
                    try {
                        component = new Component(file);
                        components.add(component);
                        addedComponents.append(component.getName()).append("  ");
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "Komponente mit dem Dateinamen \"" + file.getName() +
                                "\" konnte nicht geladen werden aufgrund eines Fehlers: Siehe folgende Fehlermeldung: ", e);
                    }
                });
        LOGGER.info("Es wurden folgende Komponenten aus dem Verzeichnis neu dazugeladen:\n " + addedComponents);
    }

    // Folgende Methoden geben zurück, ob sie erfolgreich waren oder nicht
    public boolean deployComponentById(int id){
        if (id < 0 || id >= components.size()){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        Component component = components.get(id);

        if (component.getState() != ComponentState.LOADED){
            id = components.size();
            try {
                component = new Component(component);
                components.add(component);
            } catch (Exception e) {
                LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " konnte nicht erneut deployt werden.");
            }
        }

        try {
            components.get(id).deploy(this);
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wurde erfolgreich deployt.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Beim Deployen der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public boolean startComponentById(int id){
        if (id < 0 || id >= components.size()){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        try {
            Component component = components.get(id);
            Thread thread = new Thread(() -> {
                try {
                    component.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id +
                    " konnte erfolgreich gestartet werden.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Beim Starten der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public void listComponents(){
        StringBuilder builder = new StringBuilder();
        if (components.isEmpty()){
            LOGGER.warning("Die Laufzeitumgebung hat keine Komponenten.");
            return;
        }
        for (int i = 0; i < components.size(); i++){
            Component component = components.get(i);
            builder.append("ID: ").append(i).append(", Name: ").append(component.getName()).append(", Zustand: ").append(component.getState());
            if (i < components.size() - 1) builder.append("\n");
        }
        LOGGER.info("Die Laufzeitumgebung hat folgende Komponenten: \n" + builder);
    }

    public boolean stopComponentById(int id){
        if (id < 0 || id >= components.size()){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        try {
            components.get(id).stop();
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id +
                    " konnte erfolgreich gestoppt werden.");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Beim Stoppen der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public boolean deleteComponentById(int id){
        if (id < 0 || id >= components.size()){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        Component component = components.get(id);

        if (component.getState() == ComponentState.DELETED){
            LOGGER.warning("Die Komponente \"" + component.getState() + "\" wurde schon gelöscht.");
            return false;
        }

        if (component.getState() == ComponentState.STARTED){
            stopComponentById(id);
        }

        // TODO Delete from API

        component.delete();
        components.remove(component);
        return true;
    }

    public void shutdown(){
        LOGGER.info("Fahre Laufzeitumgebung herunter ...");
        if (!isRunning()){
            LOGGER.severe("Laufzeitumgebung ist bereits beendet.");
            return;
        }

        boolean failed = false;
        for (int i = 0; i < components.size() && components.get(i).getState() == ComponentState.STARTED; i++) {
            boolean success = stopComponentById(i);
            if (!success) failed = true;
        }
        if (failed){
            LOGGER.severe("Nicht alle Komponenten konnten gestoppt werden. Laufzeitumgebung kann nicht heruntergefahren werden.");
            return;
        }

        running = false;
        components.clear();
        LOGGER.info("- - - Laufzeitumgebung erfolgreich heruntergefahren - - -");
    }
}
