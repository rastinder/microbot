package net.runelite.client.plugins.microbot.tutorialisland;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NameUtils {

    //private static final String DIRECTORY_PATH = "E:\\torrent download\\Facebook Leak [2019][533M Records][106 Countries]";
    private static final String DIRECTORY_PATH = "C:\\osrs\\user";
    private final List<File> textFiles;
    private final Map<File, List<String>> firstNameCache = new HashMap<>();
    private final Map<File, List<String>> lastNameCache = new HashMap<>();

    public NameUtils() throws IOException {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.isDirectory()) {
            throw new IOException("The path is not a directory.");
        }

        textFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles((dir, name) -> name.endsWith(".txt"))))
                .collect(Collectors.toList());

        if (textFiles.isEmpty()) {
            throw new FileNotFoundException("No text files found in the directory.");
        }

        preloadFiles();
    }

    private void preloadFiles() throws IOException {
        for (File file : textFiles) {
            List<String> firstNames = new ArrayList<>();
            List<String> lastNames = new ArrayList<>();

            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length >= 4) {
                    firstNames.add(parts[2].trim());
                    lastNames.add(parts[3].trim());
                }
            }

            firstNameCache.put(file, firstNames);
            lastNameCache.put(file, lastNames);
        }
    }

    public AbstractMap.SimpleEntry<String, String> getRandomNames() {
        while (true) {
            try {
                // Get random first name
                String firstName = getRandomNameFromCache(true);

                // Get random last name
                String lastName = getRandomNameFromCache(false);

                return new AbstractMap.SimpleEntry<>(firstName, lastName);
            } catch (Exception e) {
                // Handle exceptions by returning nulls
                e.printStackTrace();
                return new AbstractMap.SimpleEntry<>(null, null);
            }
        }
    }

    private String getRandomNameFromCache(boolean isFirstName) {
        File randomFile = textFiles.get(ThreadLocalRandom.current().nextInt(textFiles.size()));
        List<String> names = isFirstName ? firstNameCache.get(randomFile) : lastNameCache.get(randomFile);

        if (names == null || names.isEmpty()) {
            return null;
        }

        return names.get(ThreadLocalRandom.current().nextInt(names.size()));
    }
}
