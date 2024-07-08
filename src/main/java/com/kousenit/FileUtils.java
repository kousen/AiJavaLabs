package com.kousenit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {

    public static void saveBinaryFile(byte[] data, String fileName) {
        Path directory = Paths.get("src/main/resources");
        Path filePath = directory.resolve(fileName);
        try {
            Files.write(filePath, data, StandardOpenOption.CREATE_NEW);
            System.out.printf("Saved %s to %s%n", fileName, directory.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing audio to file", e);
        }
    }
}
