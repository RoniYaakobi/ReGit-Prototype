package org.roniyaakobi.objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

public interface Compressable {
    /**
     * Compress the object
     * @return The compressed object in bytes
     */
    byte[] compress();

    /**
     * Deserialize an object from id to object
     * @param id The id of the object
     * @return The deserialized object
     * @throws IOException
     */
    static RegitObject deserialize(RegitObjects.ObjectId id) throws IOException {
        try (InputStream file = Files.newInputStream(id.getPath());
             InflaterInputStream inflater = new InflaterInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            inflater.transferTo(output);

            String[] headerAndBody = output.toString().split("\0", 2);

            String header = headerAndBody[0], body = headerAndBody[1];

            String[] headerFields = header.split(" ");

            String objectTypeName = headerFields[0];

            RegitObjects.ObjectType objectType =
                    RegitObjects.ObjectType.valueOf(objectTypeName.toUpperCase());

            if (objectType == RegitObjects.ObjectType.BLOB){
                throw new IllegalArgumentException("Cannot deserialize blob object");
            }

            return objectType.deserialize(body.getBytes());
        }
    }

    /**
     * Load bytes from the objectId
     * @param objectId the object's id
     * @return the bytes from the file
     * @throws IOException
     */
    static byte[] loadObjectId(RegitObjects.ObjectId objectId) throws IOException {
        try (InputStream file = Files.newInputStream(objectId.getPath());
             InflaterInputStream inflater = new InflaterInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            inflater.transferTo(output);

            return output.toByteArray();
        }
    }
}
