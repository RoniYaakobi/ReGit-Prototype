package org.roniyaakobi.objects;

import org.roniyaakobi.utils.BytesFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.InflaterInputStream;

public interface Compressible {
    /**
     * Compress the object
     * @return The compressed object in bytes
     */
    byte[] compress();


    static RegitObject parseBytes(byte[] bytes){
        String[] headerAndBody = BytesFormatter.bytesToString(bytes).split("\0", 2);

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
