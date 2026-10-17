package org.roniyaakobi.objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class Blob extends RegitObject {
    public record StreamData(byte[] bytes, int length) {}
    private final Path path;

    private RegitObjects.ObjectId objectId = null;
    private final long size;

    private Blob(Path path, long size) {
        this.path = path;
        this.size = size;

        this.objectId = hash();
    }

    public Path getPath(){
        return path;
    }

    public long getSize(){
        return size;
    }

    public static Blob fromPath(Path path) throws IOException {
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

    public byte[] compress(){
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {


            deflater.write(buildHeaderBytes());

            streamFileContents(
                    streamData -> {
                        try {
                            deflater.write(streamData.bytes(), 0, streamData.length());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            deflater.finish();

            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public  RegitObjects.ObjectId hash() {
        if (objectId != null){
            return objectId;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] headerBytes = buildHeaderBytes();

            digest.update(headerBytes);

            streamFileContents(
                    streamData ->
                            digest.update(streamData.bytes(), 0 ,streamData.length()));

            return new RegitObjects.ObjectId(digest.digest(), RegitObjects.ObjectType.BLOB);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }

    public byte[] getAllFileBytes() throws IOException {
        return Files.readAllBytes(path);
    }
}
