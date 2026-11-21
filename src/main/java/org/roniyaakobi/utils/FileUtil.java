package org.roniyaakobi.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    /**
     * Write a buffer of data to a file safely, including creating the directory if it doesnt exist
     * @param path the file
     * @param data the data
     * @throws IOException
     */
    public static void writeFile(Path path, byte[] data) throws IOException {
        createFileIfNotExists(path);

        Files.write(path, data);
    }

    /**
     * Create a directory safely, including parents if needed
     * @param directory the directory
     * @throws IOException
     */
    public static void createDirectory(Path directory) throws IOException {
        System.out.println(directory);
        if (!Files.exists(directory.getParent())){
            createDirectory(directory.getParent());
        }

        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
    }

    /**
     * A record read from a file: a text header and the body it describes
     * @param header the header text (without the null terminator)
     * @param body the body bytes
     */
    public record LengthPrefixedRecord(String header, byte[] body) {}

    /**
     * Read a record of the form "&lt;fields...&gt; &lt;length&gt;\0&lt;body&gt;" where length is the
     * last space separated header field and is the number of body bytes
     * @param file the file to read from
     * @param offset the offset of the record in the file
     * @return the header and the body of the record
     * @throws IOException
     */
    public static LengthPrefixedRecord readLengthPrefixedRecord(File file, long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream()) {

            raf.seek(offset);

            int currentByte;
            while ((currentByte = raf.read()) != 0) {
                if (currentByte == -1) {
                    throw new EOFException("Reached end of file while reading record header at offset " + offset);
                }
                headerBuffer.write(currentByte);
            }

            String header = headerBuffer.toString();
            String[] fields = header.split(" ");
            int length = Integer.parseInt(fields[fields.length - 1]);

            byte[] body = new byte[length];
            raf.readFully(body);

            return new LengthPrefixedRecord(header, body);
        }
    }

    public static void createFileIfNotExists(Path path) throws IOException {
        if (!Files.exists(path.getParent())){
            createDirectory(path.getParent());
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }
}
