package org.hbrs.ooka.uebung2_3.runtimeEnvironment;

import lombok.Getter;
import org.hbrs.ooka.uebung2_3.component.Component;
import org.hbrs.ooka.uebung2_3.component.ComponentState;
import org.hbrs.ooka.uebung2_3.services.logger.ILogger;
import org.hbrs.ooka.uebung2_3.services.logger.RuntimeEnvironmentLogger;
import org.hbrs.ooka.uebung2_3.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class RuntimeEnvironment {
    public static final ILogger LOGGER = new RuntimeEnvironmentLogger();
    @Getter
    private final RuntimeEnvironmentAPI api = new RuntimeEnvironmentAPI();

    /**
     * Die laufende Identifikationsnummer ergibt sich als Key der HashMap.
     */
    private HashMap<Integer, Component> components = new HashMap<>();

    @Getter
    private int nextId = 0;
    @Getter
    private boolean running = false;

    public RuntimeEnvironment(){
        dumpConfig();
    }

    private void dumpConfig(){
        try {
            Files.deleteIfExists(Paths.get("config/LZU-config.json"));
            FileUtil.saveToJson("config/LZU-config.json", new RuntimeEnvironmentConfig(components.values()));
        } catch (IOException e) {
            LOGGER.severe("Beim Schreiben der RuntimeEnvironmentConfig ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
        }
    }

    public void loadConfig(String path){
        RuntimeEnvironmentConfig config;
        try {
            config = FileUtil.loadFromJson(path, RuntimeEnvironmentConfig.class);
        } catch (IOException e) {
            LOGGER.severe("Beim Laden der RuntimeEnvironmentConfig ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return;
        }
        if (config.getComponents().size() != config.getLoadedOrDeployed().size()){
            LOGGER.severe("Die Konfigurationsdatei " + path + " ist fehlerhaft.");
            return;
        }
        LOGGER.info("Die Konfiguration aus der Konfigurationsdatei " + path + " wird übernommen ...");

        if (!components.isEmpty()){
            deleteAllComponents();
        }

        config.getComponents().forEach(this::loadComponent);
        for (int i = 0; i < config.getLoadedOrDeployed().size(); i++){
            if (config.getLoadedOrDeployed().get(i).equals("DEPLOYED")){
                deployComponentById(i);
            }
        }

        LOGGER.info("Die Konfigurationsdatei " + path + " wurde erfolgreich geladen.");
    }

    public void start(){
        LOGGER.info("Starte Laufzeitumgebung ...");
        if (isRunning()){
            LOGGER.severe("Laufzeitumgebung ist bereits gestartet.");
            return;
        }

        running = true;
        LOGGER.info("- - - Laufzeitumgebung erfolgreich gestartet - - -");
    }

    public void loadComponent(String path){
        if (!isRunning()){
            LOGGER.warning("Laufzeitumgebung ist noch nicht gestartet worden.");
            return;
        }
        try {
            LOGGER.info("Komponente mit dem Dateinamen \"" + path + "\" wird geladen ...");
            Component component = new Component(path);
            components.put(nextId++, component);
            dumpConfig();
            LOGGER.info("Komponente mit dem Dateinamen \"" + path + "\" konnte erfolgreich geladen werden.");
        } catch (Exception e) {
            LOGGER.severe("Komponente mit dem Dateinamen \"" + path +
                    "\" konnte nicht geladen werden aufgrund eines Fehlers: Siehe folgende Fehlermeldung: ", e);
        }
    }

    public void loadAllComponents(String compDir){
        if (!isRunning()){
            LOGGER.warning("Laufzeitumgebung ist noch nicht gestartet worden.");
            return;
        }
        LOGGER.info("Alle Komponenten aus dem Verzeichnis \"" + compDir + "\" werden geladen ...");
        Arrays.stream(Objects.requireNonNull(Paths.get(compDir).toFile().listFiles()))
            .filter(file -> file.getName().endsWith(".jar")).map(File::getName).forEach(fileName -> {
                Component component;
                try {
                    component = new Component(compDir + "/" + fileName);
                    components.put(nextId++, component);
                } catch (MalformedURLException e) {
                    LOGGER.sendLog(Level.SEVERE, "Komponente mit dem Dateinamen \"" + fileName +
                            "\" konnte nicht geladen werden aufgrund eines Fehlers: Siehe folgende Fehlermeldung: ", e);
                }
        });
        dumpConfig();
        LOGGER.info("Alle Komponenten aus dem Verzeichnis \"" + compDir + "\" wurden erfolgreich geladen.");
    }

    // Folgende Methoden geben zurück, ob sie erfolgreich waren oder nicht
    public boolean deployComponentById(int id){
        if (!components.containsKey(id)){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        Component component = components.get(id);

        if (component.getState() != ComponentState.LOADED){
            id = components.size();
            try {
                component = new Component(component);
                components.put(nextId++, component);
            } catch (Exception e) {
                LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " konnte nicht erneut deployt werden.");
            }
        }

        try {
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wird deployt.");
            components.get(id).deploy(this, id);
            dumpConfig();
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wurde erfolgreich deployt.");
            return true;
        } catch (Exception e) {
            LOGGER.sendLog(Level.SEVERE, "Beim Deployen der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public boolean startComponentById(int id){
        if (!components.containsKey(id)){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        try {
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wird gestartet.");
            Component component = components.get(id);
            component.start();
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id +
                    " konnte erfolgreich gestartet werden.");
            return true;
        } catch (Exception e) {
            LOGGER.sendLog(Level.SEVERE, "Beim Starten der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public boolean stopComponentById(int id){
        if (!components.containsKey(id)){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        try {
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wird gestoppt.");
            components.get(id).stop();
            LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id +
                    " konnte erfolgreich gestoppt werden.");
            return true;
        } catch (Exception e) {
            LOGGER.sendLog(Level.SEVERE, "Beim Stoppen der Komponente \"" + components.get(id).getName() +
                    "\" mit der ID " + id + " ist ein Fehler aufgetreten. Siehe folgende Fehlermeldung: ", e);
            return false;
        }
    }

    public boolean deleteComponentById(int id){
        if (!components.containsKey(id)){
            LOGGER.warning("Eine Komponente mit der ID " + id + " konnte nicht gefunden werden.");
            return false;
        }

        Component component = components.get(id);

        if (component.getState() == ComponentState.DELETED){
            LOGGER.warning("Die Komponente \"" + component.getState() + "\" wurde schon gelöscht.");
            return false;
        }
        LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id + " wird gelöscht.");
        if (component.getState() == ComponentState.STARTED){
            stopComponentById(id);
        }

        // TODO Delete from API

        component.delete();
        components.remove(component);
        dumpConfig();
        LOGGER.info("Die Komponente \"" + components.get(id).getName() + "\" mit der ID " + id +
                " konnte erfolgreich gelöscht werden.");
        return true;
    }

    public void deleteAllComponents(){
        LOGGER.info("Lösche alle vorhandenen Komponenten ...");
        components.keySet().forEach(this::deleteComponentById);
        nextId = 0;
        LOGGER.info("Alle vorhandenen Komponenten wurden erfolgreich gelöscht.");
    }

    public void listComponents(){
        StringBuilder builder = new StringBuilder();
        if (components.isEmpty()){
            LOGGER.warning("Die Laufzeitumgebung hat keine Komponenten.");
            return;
        }
        for (int id : components.keySet()){
            Component component = components.get(id);
            builder.append("ID: ").append(id).append(", Name: ").append(component.getName()).append(", Zustand: ").append(component.getState());
            builder.append("\n");
        }

        LOGGER.info("Die Laufzeitumgebung hat folgende Komponenten: \n" + builder.substring(0, builder.length()-1));
    }

    public void shutdown(){
        LOGGER.info("Fahre Laufzeitumgebung herunter ...");
        if (!isRunning()){
            LOGGER.severe("Laufzeitumgebung ist bereits beendet.");
            return;
        }

        deleteAllComponents();

        running = false;
        components.clear();
        LOGGER.info("- - - Laufzeitumgebung erfolgreich heruntergefahren - - -");
    }
}
