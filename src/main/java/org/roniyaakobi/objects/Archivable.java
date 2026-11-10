package org.roniyaakobi.objects;

import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Archivable extends Compressible {
    public static final Path INDEX_PATH =
            Path.of("playground/.regit/objectStore/index.map");

    public static final Path ARCHIVE_PATH =
            Path.of("playground/.regit/objectStore/archive.obj").toAbsolutePath().normalize();


    public static final Map<RegitObjects.ObjectId, Long> OBJECT_MAP = getObjectMap();

    public static final byte DELIMETER_BYTE = 3;
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

    default void archive() throws IOException {

        if (OBJECT_MAP.containsKey(getObjectId())){
            return;
        }

        byte[] compressed = compress();

        FileUtil.createFileIfNotExists(ARCHIVE_PATH);

        RandomAccessFile file = new RandomAccessFile(ARCHIVE_PATH.toString(), "rw");
        Long offset = file.length();

        file.seek(file.length());


        file.write(compressed, 0, compressed.length);
        file.writeByte(DELIMETER_BYTE);
        OBJECT_MAP.put(getObjectId(), offset);
    }

    static RegitObject unarchive(RegitObjects.ObjectId objectId) throws IOException {
        return Compressible.parseBytes(loadBytes(objectId));
    }

    static byte[] loadBytes(RegitObjects.ObjectId objectId) throws IOException {
        long objectOffset = OBJECT_MAP.get(objectId);

        return  FileUtil.readBytesUntilDelimiter(ARCHIVE_PATH.toFile(), objectOffset, DELIMETER_BYTE);
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

