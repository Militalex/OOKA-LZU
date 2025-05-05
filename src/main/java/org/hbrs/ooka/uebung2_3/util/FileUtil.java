package org.hbrs.ooka.uebung2_3.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexander Ley
 * @version 2.0
 *
 * Basic Methods to work with Files.
 *
 */
public final class FileUtil {

    /**
     * If not null Errors will be logged here.
     */
    public static Logger logger = null;

    /**
     * fp := from Path String
     * @param path e.g. "C:/Users/Hans/Dokumente/..." (absolute Path)
     *             or e.g. "src/assets/test.txt" Path after the workspace - folder. (relative Path)
     *             It can point to directories or files.
     * @return Returns a Path from a given path.
     */
    public static Path getPathfp(@NotNull String path){
        return Paths.get(path);
    }

    /**
     * File, the url is pointing to, have to be exists. Otherwise it will throw NullPointerException
     * @param url e.g. "assets/textures/dice.png". Path after the src - folder.
     * @return Returns an URI to create a file or return null when URI Syntax is wrong.
     */
    public static URI getURI(@NotNull String url) {
        try {
            return Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(url)).toURI();
        } catch (URISyntaxException e) {
            if (logger != null) logger.log(Level.SEVERE, "URI Syntax is wrong: " + url, e);
            else {
                System.err.println("URI Syntax is wrong: " + url);
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * @param path e.g. "C:/Users/Hans/Dokumente/..." (absolute Path)
     *             or e.g. "src/assets/test.txt" Path after the workspace - folder. (relative Path)
     *             It can point to directories or files.
     * @return Returns false if file could not created.
     */
    public static void createIfNotExists(@NotNull String path) throws IOException {
        createIfNotExists(Paths.get(path));
    }

    /**
     * Try to create a file (or a directory if @param path is a directory) if non existent. It also try to generate the directories the file is in when not existing.
     * @return Returns false if file could not created.
     */
    public static void createIfNotExists(@NotNull Path path) throws IOException {
        createIfNotExists(path.toFile());
    }

    /**
     * Try to create a file (or a directory if @param file is a directory) if non-existent.
     * It also tries to generate the directories the file is in when not existing.
     * @return Returns false if file could not create.
     */
    public static void createIfNotExists(@NotNull File file) throws IOException{
        if (!file.exists()) {
            if (file.isDirectory()) {
                Files.createDirectories(file.toPath());
            }
            try {
                Files.createFile(file.toPath());
            }
            catch (IOException ioException){
                Files.createDirectories(file.toPath().getParent());
                Files.createFile(file.toPath());
            }
        }
    }

    /**
     * @param url e.g. "assets/textures/dice.png".
     * @return Returns the content of a file as String or "" if the file does not exists.
     */
    public static String getFileContent(@NotNull String url) {
        try {
            Scanner sc = new Scanner(Paths.get(url).toFile());
            StringBuilder text = new StringBuilder();
            while (sc.hasNext()){
                text.append(sc.nextLine()).append("\n");
            }
            return text.toString();
        }
        catch (FileNotFoundException e) {
            if (logger != null) logger.log(Level.SEVERE, "File: " + url + " cannot found. ", e);
            else {
                System.err.println("File: " + url + " cannot found. ");
                e.printStackTrace();
            }
            return "";
        }
    }

    /**
     *
     * @param file Input file.
     * @return Returns the content of a file as String.
     */
    public static String getFileContent(@NotNull File file) {
        try {
            Scanner sc = new Scanner(file);
            StringBuilder text = new StringBuilder();
            while (sc.hasNext()){
                text.append(sc.nextLine()).append("\n");
            }
            return text.toString();
        }
        catch (FileNotFoundException e) {
            if (logger != null) logger.log(Level.SEVERE, "File: " + file.getName() + " cannot found. ", e);
            else {
                System.err.println("File: " + file.getName() + " cannot found. ");
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Checks if a file is empty.
     */
    public static boolean isEmpty(Path path){
        try {
            return Files.readAllLines(path).isEmpty();
        }
        catch (IOException ioException) {
            throw new IllegalArgumentException("Something went wrong with: " + path, ioException);
        }
    }

    /**
     * Saves an object to json file using Gson. File will be created if it not exists.
     * @param path e.g. "assets/textures/dice.json". Path after the src - folder.
     * @param obj Object which should be saved.
     */
    public static void saveToJson(String path, Object obj) throws IOException {
       saveToJson(Paths.get(path), obj);
    }

    /**
     * Saves an object to json file using Gson. File will be created if it not exists.
     * @param file File, where obj have to be saved.
     * @param obj Object which should be saved.
     */
    public static void saveToJson(File file, Object obj) throws IOException {
        saveToJson(file.toPath(), obj);
    }


    /**
     * Saves an object to json file using Gson. File will be created if it not exists.
     * @param path File, where obj have to be saved.
     * @param obj Object which should be saved.
     */
    public static void saveToJson(Path path, Object obj) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        createIfNotExists(path);
        Files.writeString(path, gson.toJson(obj), StandardOpenOption.WRITE);
    }

    /**
     * @param path e.g. "assets/textures/dice.json". Path (to a json file) after the src - folder.
     * @param tClass Class of the loading object.
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(String path, Class<T> tClass) throws IOException {
        return loadFromJson(Paths.get(path), tClass);
    }

    /**
     * @param file Json file.
     * @param tClass Class of the loading object.
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(File file, Class<T> tClass) throws IOException {
        return loadFromJson(file.toPath(), tClass);
    }

    /**
     * @param path Json file.
     * @param tClass Class of the loading object.
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(Path path, Class<T> tClass) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(Files.readString(path), tClass);
    }

    /**
     * For Objects using generics.
     * @param file e.g. "assets/textures/dice.json". Path (to a json file) after the src - folder.
     * @param token have to be new TypeToken<>(){}
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(File file, TypeToken<T> token) throws IOException {
        return loadFromJson(file.toPath(), token);
    }

    /**
     * For Objects using generics.
     * @param path e.g. "assets/textures/dice.json". Path (to a json file) after the src - folder.
     * @param token have to be new TypeToken<>(){}
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(String path, TypeToken<T> token) throws IOException {
        return loadFromJson(Paths.get(path), token);
    }

    /**
     * For Objects using generics.
     * @param path e.g. "assets/textures/dice.json". Path (to a json file) after the src - folder.
     * @param token have to be new TypeToken<>(){}
     * @param <T> Object that was saved to json.
     * @return Returns object from json file using Gson. Returns null if loading fails.
     */
    public static <T> T loadFromJson(Path path, TypeToken<T> token) throws IOException {
        final Gson gson = new Gson();
        return gson.fromJson(Files.readString(path), token.getType());
    }
}
