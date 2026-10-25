package org.roniyaakobi.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWritingUtil {

    /**
     * Write a buffer of data to a file safely, including creating the directory if it doesnt exist
     * @param path the file
     * @param data the data
     * @throws IOException
     */
    public static void writeFile(Path path, byte[] data) throws IOException {
        if (!Files.exists(path.getParent())){
            createDirectory(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Files.write(path, data);
    }

    /**
     * Create a directory safely, including parents if needed
     * @param directory the directory
     * @throws IOException
     */
    public static void createDirectory(Path directory) throws IOException {
        if (!Files.exists(directory.getParent())){
            createDirectory(directory.getParent());
        }

        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
    }
}
