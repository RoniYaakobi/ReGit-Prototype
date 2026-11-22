package org.roniyaakobi.objects;

import org.roniyaakobi.utils.FileUtil;
import org.roniyaakobi.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;

public final class Blob extends RegitObject {
    public record StreamData(byte[] bytes, int length) {}

    private final Path path;

    private final long size;

    private Blob(Path path, long size) throws IOException {
        Logger.log("Created blob object for: " + path.toString() + "\n");
        this.path = path;
        this.size = size;

        getObjectId();

        store();
    }

    /**
     * Get the size of the blob file
     * @return the size of the blob file
     */
    public long getSize(){
        return size;
    }

    /**
     * Create a blob from a given path
     * @param path The path of the blob source
     * @return A blob object
     * @throws IOException
     */
    public static Blob fromPath(Path path) throws IOException {
        return new Blob(path, Files.size(path));
    }

    /**
     * Stream 8kb sections into a consumer
     * @param streamDataConsumer the consumer for the bytes of the file
     */
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

    @Override
    public byte[] compressBody(){
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {

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

    @Override
    public RegitObjects.ObjectType getType() {
        return RegitObjects.ObjectType.BLOB;
    }

    /**
     * Restore a blob's contents from the archive to a file
     * @param target the file to write
     * @param id the id of the blob
     * @throws IOException
     */
    public static void load(Path target, RegitObjects.ObjectId id) throws IOException {
        Archivable.ArchiveRecord record = Archivable.loadRecord(id);

        if (record.type() != RegitObjects.ObjectType.BLOB){
            throw new IllegalArgumentException("Object " + id.getHex() + " is not a blob");
        }

        FileUtil.writeFile(target, Compressible.inflate(record.compressed()));
    }
}
