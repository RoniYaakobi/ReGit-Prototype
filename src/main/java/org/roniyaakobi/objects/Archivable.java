package org.roniyaakobi.objects;

import org.roniyaakobi.utils.FileUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface Archivable extends Compressible {
    public static final Path INDEX_PATH =
            Path.of("playground/.regit/objectStore/index.map");

    public static final Path ARCHIVE_PATH =
            Path.of("playground/.regit/objectStore/archive.obj").toAbsolutePath().normalize();


    public static final Map<RegitObjects.ObjectId, Long> OBJECT_MAP = getObjectMap();

    /**
     * A stored object record: its type and its compressed body
     * @param type the object type from the record header
     * @param compressed the compressed body
     */
    record ArchiveRecord(RegitObjects.ObjectType type, byte[] compressed) {}

    static Map<RegitObjects.ObjectId, Long> getObjectMap() {
        Map<RegitObjects.ObjectId, Long> objectMap = new HashMap<>();

        try {
            FileUtil.createFileIfNotExists(INDEX_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Try-with-resources automatically closes the stream
        try (Stream<String> lines = Files.lines(INDEX_PATH)) {
            lines.forEach(
                    line ->
                            {
                                objectMap.put(
                                        RegitObjects.ObjectId.fromHex(line.split(":")[0],null),
                                        Long.parseLong(line.split(":")[1])
                                );
                            }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objectMap;
    }

    /**
     * Append the object to the archive as "&lt;type&gt; &lt;compressedLength&gt;\0&lt;compressedBody&gt;"
     * @throws IOException
     */
    default void archive() throws IOException {

        if (OBJECT_MAP.containsKey(getObjectId())){
            return;
        }

        byte[] record = compress();

        FileUtil.createFileIfNotExists(ARCHIVE_PATH);

        try (RandomAccessFile file = new RandomAccessFile(ARCHIVE_PATH.toString(), "rw")) {
            long offset = file.length();

            file.seek(offset);
            file.write(record, 0, record.length);

            OBJECT_MAP.put(getObjectId(), offset);
        }
    }

    static RegitObject unarchive(RegitObjects.ObjectId objectId) throws IOException {
        ArchiveRecord record = loadRecord(objectId);

        return Compressible.parse(record.type(), Compressible.inflate(record.compressed()));
    }

    /**
     * Read the record of an object from the archive
     * @param objectId the id of the object
     * @return the type and compressed body of the object
     * @throws IOException
     */
    static ArchiveRecord loadRecord(RegitObjects.ObjectId objectId) throws IOException {
        Long objectOffset = OBJECT_MAP.get(objectId);

        if (objectOffset == null){
            throw new IllegalArgumentException("Object " + objectId.getHex() + " is not in the archive");
        }

        FileUtil.LengthPrefixedRecord record =
                FileUtil.readLengthPrefixedRecord(ARCHIVE_PATH.toFile(), objectOffset);

        String typeName = record.header().split(" ")[0];

        return new ArchiveRecord(
                RegitObjects.ObjectType.valueOf(typeName.toUpperCase()),
                record.body()
        );
    }

    /**
     * Load the inflated body of an object from the archive
     * @param objectId the id of the object
     * @return the inflated body
     * @throws IOException
     */
    static byte[] loadBytes(RegitObjects.ObjectId objectId) throws IOException {
        return Compressible.inflate(loadRecord(objectId).compressed());
    }

    RegitObjects.ObjectId getObjectId();

    static void storeMap(){
        try (FileOutputStream file = new FileOutputStream(INDEX_PATH.toFile())) {
            FileUtil.createDirectory(INDEX_PATH.getParent());

            Map.Entry<RegitObjects.ObjectId, Long>[] entries = OBJECT_MAP.entrySet().toArray(new Map.Entry[0]);

            for (Map.Entry<RegitObjects.ObjectId, Long> entry : entries){
                file.write(entry.getKey().getHex().getBytes());
                file.write(":".getBytes());
                file.write(String.valueOf(entry.getValue()).getBytes());
                file.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
