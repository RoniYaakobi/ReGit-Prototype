package org.roniyaakobi.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWritingUtil {
    public static void writeFile(Path path, byte[] data) throws IOException {
        if (!Files.exists(path.getParent())){
            createDirectory(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Files.write(path, data);
    }

    public static void createDirectory(Path directory) throws IOException {
        if (!Files.exists(directory.getParent())){
            createDirectory(directory.getParent());
        }

        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
    }
}
