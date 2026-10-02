package org.roniyaakobi.objects;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class Blob {
    public record StreamData(byte[] bytes, int length) {}
    private final Path path;
    private final long size;

    private Blob(Path path, long size) {
        this.path = path;
        this.size = size;
    }

    public Path getPath(){
        return path;
    }

    public long getSize(){
        return size;
    }

    public static Blob from(Path path) throws IOException {
        return new Blob(path, Files.size(path));
    }

    public byte[] buildHeaderBytes(){
        String header = "blob " + getSize() + "\0";
        return header.getBytes();
    }

    public void streamFileContents(Consumer<StreamData> streamDataConsumer){
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];

            for (int n = in.read(buffer); n != -1; n = in.read(buffer)) {
                streamDataConsumer.accept(new StreamData(buffer, n));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read bytes from file " + path, e);
        }
    }
}
